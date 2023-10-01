package com.aston.bankingservice.service;

import com.aston.bankingservice.exceptions.AccountValidationException;
import com.aston.bankingservice.exceptions.ApiException;
import com.aston.bankingservice.exceptions.EntityNotFoundException;
import com.aston.bankingservice.model.Account;
import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountMapper;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    @Value("${pin.regex}")
    private String pinRegex;

    @Override
    public AccountDto create(AccountNewDto accountNewDto) {
        if (!Pattern.matches(pinRegex, accountNewDto.getPin())) {
            log.warn("AccountServiceImpl.create invoke with incorrect pin={}", accountNewDto.getPin());
            throw new AccountValidationException("Account has incorrect pin.");
        }

        Account account = accountMapper.accountFromNewDto(accountNewDto);
        account.setMoney(BigDecimal.ZERO);

        log.info("AccountServiceImpl.create create entity in repository.");
        return accountMapper.accountToDto(accountRepository.save(account));
    }

    @Override
    public List<AccountDto> getAll() {
        List<Account> accounts = accountRepository.getAll();

        log.info("AccountServiceImpl.getAll invoke");
        return accounts.stream()
                .map(accountMapper::accountToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDto depositAction(String action, float money, int accountNumber, String pin) {
        Account account = getOrThrow(accountNumber);
        pinValidateOrThrow(account, pin);
        BigDecimal moneyAcc = account.getMoney();

        switch (action) {
            case "put":
                account.setMoney(moneyAcc.add(BigDecimal.valueOf(money)));
                log.info("AccountServiceImpl.depositAction put money in deposit.");
                break;
            case "withdraw":
                availabilityOfMoney(account, BigDecimal.valueOf(money));
                account.setMoney(moneyAcc.subtract(BigDecimal.valueOf(money)));
                log.info("AccountServiceImpl.depositAction withdraw money in deposit.");
                break;
            default:
                throw new ApiException("Unsupported action with deposit");
        }

        accountRepository.update(account);

        log.info("AccountServiceImpl.depositAction update entity in repository.");
        return accountMapper.accountToDto(account);
    }

    @Override
    @Transactional
    public List<AccountDto> transferMoney(int accountNumber, float money, int userAccountNumber, String pin) {
        Account sender = getOrThrow(userAccountNumber);
        Account recipient = getOrThrow(accountNumber);
        pinValidateOrThrow(sender, pin);
        availabilityOfMoney(sender, BigDecimal.valueOf(money));

        BigDecimal moneyAcc = sender.getMoney();
        sender.setMoney(moneyAcc.subtract(BigDecimal.valueOf(money)));

        moneyAcc = recipient.getMoney();
        recipient.setMoney(moneyAcc.add(BigDecimal.valueOf(money)));

        accountRepository.update(sender);
        accountRepository.update(recipient);

        List<AccountDto> result = new ArrayList<>();
        result.add(accountMapper.accountToDto(sender));
        result.add(accountMapper.accountToDto(recipient));

        return result;
    }

    @Override
    public AccountDto getByNumber(int accountNumber) {
        Account account = getOrThrow(accountNumber);

        log.info("AccountServiceImpl.getByNumber invoke");
        return accountMapper.accountToDto(account);
    }

    private Account getOrThrow(int accountNumber) {
        return accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found, accountNumber=" + accountNumber));
    }

    private void pinValidateOrThrow(Account account, String pin) {
        if (!account.getPin().equals(pin)) {
            log.warn("AccountServiceImpl invoke with incorrect pin");
            throw new AccountValidationException("Insert incorrect PIN.");
        }
    }

    private void availabilityOfMoney(Account account, BigDecimal money) {
        if (money.compareTo(account.getMoney()) > 0) {
            throw new AccountValidationException("There is not enough money in the account.");
        }
    }
}

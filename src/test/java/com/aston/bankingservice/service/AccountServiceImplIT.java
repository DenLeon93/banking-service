package com.aston.bankingservice.service;

import com.aston.bankingservice.exceptions.AccountValidationException;
import com.aston.bankingservice.model.Account;
import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AccountServiceImplIT {

    private final AccountService accountService;

    private AccountNewDto accountNewDto;
    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void beforeEach() {
        account = Account.builder()
                .name("Username")
                .pin("1234")
                .accountNumber(1)
                .money(BigDecimal.ZERO)
                .build();

        accountNewDto = AccountNewDto.builder()
                .name("Username")
                .pin("1234").build();

        accountDto = AccountDto.builder()
                .name("Username")
                .accountNumber(1)
                .money(BigDecimal.ZERO).build();
        ReflectionTestUtils.setField(accountService, "pinRegex", "\\d{4}");
        accountService.create(accountNewDto);
    }

    @Test
    void create_whenInvoke_thenReturnAccountDto() {
        accountDto = accountService.create(accountNewDto);

        assertEquals(accountDto.getName(), account.getName());
        assertTrue(accountDto.getAccountNumber() > 0);
        assertEquals(BigDecimal.ZERO.toString(), accountDto.getMoney().toString());
    }

    @Test
    void create_whenInvokeWithInvalidPIN_thenThrow() {
        accountNewDto.setPin("asd");

        assertThrows(AccountValidationException.class,
                () -> accountService.create(accountNewDto));
    }

    @Test
    void getAll_whenInvoke_thenListNotEmpty() {
        List<AccountDto> accountDtoList = accountService.getAll();

        assertFalse(accountDtoList.isEmpty());
    }

    @Test
    void depositAction_whenInvokePut_thenReturnAccountDto() {
        String action = "put";
        float money = 12.0F;
        AccountDto accountDto1 = accountService.create(accountNewDto);

         accountService.depositAction(
                action,
                money,
                accountDto1.getAccountNumber(),
                accountNewDto.getPin());

        AccountDto accountDtoAfterPutMoney = accountService.getByNumber(accountDto1.getAccountNumber());
        assertEquals(money, accountDtoAfterPutMoney.getMoney().floatValue());
    }

    @Test
    void depositAction_whenInvokeWithdraw_thenReturnAccountDto() {
        String action = "put";
        float money = 12.0F;
        AccountDto accountDto1 = accountService.create(accountNewDto);

        accountService.depositAction(
                action,
                money,
                accountDto1.getAccountNumber(),
                accountNewDto.getPin());

        action = "withdraw";
        money = 6.0F;
        accountService.depositAction(
                action,
                money,
                accountDto1.getAccountNumber(),
                accountNewDto.getPin());

        AccountDto accountDtoAfterPutMoney = accountService.getByNumber(accountDto1.getAccountNumber());
        assertEquals(money, accountDtoAfterPutMoney.getMoney().floatValue());
    }

    @Test
    void transferMoney_whenInvoke_thenReturnListAccountDto() {
        AccountNewDto senderNewDto = accountNewDto;
        AccountNewDto recipientNewDto = AccountNewDto.builder()
                .name("recipient")
                .pin("5678").build();
        float money = 12.0F;
        String action = "put";

        AccountDto senderDto = accountService.create(senderNewDto);
        AccountDto recipientDto = accountService.create(recipientNewDto);

        accountService.depositAction(
                action,
                money,
                senderDto.getAccountNumber(),
                accountNewDto.getPin());

        List<AccountDto> accountDtoList = accountService.transferMoney(recipientDto.getAccountNumber(),
                money,
                senderDto.getAccountNumber(),
                accountNewDto.getPin());

        assertFalse(accountDtoList.isEmpty());
        assertEquals(0.0F, accountDtoList.get(0).getMoney().floatValue());
        assertEquals(money, accountDtoList.get(1).getMoney().floatValue());
    }

    @Test
    void getByNumber_whenInvoke_thenReturnAccountDto() {
        accountDto = accountService.create(accountNewDto);

        AccountDto accountDtoActual = accountService.getByNumber(accountDto.getAccountNumber());

        assertEquals(accountDto.getName(), accountDtoActual.getName());
    }
}
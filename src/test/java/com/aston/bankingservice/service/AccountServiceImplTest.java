package com.aston.bankingservice.service;

import com.aston.bankingservice.exceptions.AccountValidationException;
import com.aston.bankingservice.exceptions.ApiException;
import com.aston.bankingservice.exceptions.EntityNotFoundException;
import com.aston.bankingservice.model.Account;
import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountMapper;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Captor
    private ArgumentCaptor<Account> accountArgumentCaptor;

    private AccountNewDto accountNewDto;
    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void beforeEach() {
        account = Account.builder()
                .name("Username")
                .pin("1234")
                .accountNumber(4567)
                .money(BigDecimal.ZERO)
                .build();

        accountNewDto = AccountNewDto.builder()
                .name("Username")
                .pin("1234").build();

        accountDto = AccountDto.builder()
                .name("Username")
                .accountNumber(4567)
                .money(BigDecimal.ZERO).build();
        ReflectionTestUtils.setField(accountService, "pinRegex", "\\d{4}");
    }

    @Test
    void create_whenInvoke_thenReturnAccountDto() {
        when(accountMapper.accountFromNewDto(accountNewDto)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        AccountDto accountDto1 = accountService.create(accountNewDto);

        assertEquals(accountDto1, accountDto);
        verify(accountRepository).save(account);
    }

    @Test
    void create_whenInvoke_thenReturnAccountDtoAndAssignedMoney() {
        Account accountWithoutMoney = Account.builder()
                .name("Username")
                .pin("1234")
                .accountNumber(4567)
                .build();
        when(accountMapper.accountFromNewDto(accountNewDto)).thenReturn(accountWithoutMoney);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        AccountDto accountDto1 = accountService.create(accountNewDto);

        assertEquals(accountDto1, accountDto);
        verify(accountRepository).save(account);
        verify(accountRepository).save(accountArgumentCaptor.capture());

        Account savingAccount = accountArgumentCaptor.getValue();
        assertEquals(BigDecimal.ZERO, savingAccount.getMoney());
    }

    @Test
    void create_whenInvokeWithInvalidPIN_thenThrow() {
        accountNewDto = AccountNewDto.builder()
                .name("Username")
                .pin("abcde").build();

        assertThrows(AccountValidationException.class,
                () -> accountService.create(accountNewDto));
        verify(accountRepository, never()).save(account);
    }

    @Test
    void getAll_whenInvoke_thenReturnListAccountDto() {
        List<Account> accounts = List.of(account);
        List<AccountDto> expectedList = List.of(accountDto);
        when(accountRepository.getAll()).thenReturn(accounts);
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        List<AccountDto> actualList = accountService.getAll();

        assertEquals(actualList.size(), expectedList.size());
        assertEquals(actualList.get(0), expectedList.get(0));
    }

    @Test
    void getAll_whenInvoke_thenReturnEmptyList() {
        List<Account> accounts = Collections.emptyList();
        when(accountRepository.getAll()).thenReturn(accounts);

        List<AccountDto> actualList = accountService.getAll();

        assertTrue(actualList.isEmpty());
    }

    @Test
    void depositAction_whenInvokeWithPutAction_thenReturnAccountDto() {
        String action = "put";
        float money = 12.0F;
        int accountNumber = 4567;
        String pin = "1234";
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        AccountDto updatedAccountDto = accountService.depositAction(action, money, accountNumber, pin);

        assertEquals(accountDto, updatedAccountDto);

        verify(accountRepository).update(accountArgumentCaptor.capture());
        Account updateAccount = accountArgumentCaptor.getValue();
        assertEquals(BigDecimal.valueOf(money), updateAccount.getMoney());
    }

    @Test
    void depositAction_whenInvokeWithWithdrawAction_thenReturnAccountDto() {
        String action = "withdraw";
        float money = 12.0F;
        int accountNumber = 4567;
        String pin = "1234";
        account.setMoney(BigDecimal.valueOf(120.0F));
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        AccountDto updatedAccountDto = accountService.depositAction(action, money, accountNumber, pin);

        assertEquals(accountDto, updatedAccountDto);

        verify(accountRepository).update(accountArgumentCaptor.capture());
        Account updateAccount = accountArgumentCaptor.getValue();
        assertTrue(updateAccount.getMoney().compareTo(BigDecimal.valueOf(120.0F)) < 0);
    }

    @Test
    void depositAction_whenInvokeWithUnsupportedAction_thenThrowApiException() {
        String action = "unsupported";
        float money = 12.0F;
        int accountNumber = 4567;
        String pin = "1234";
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));

        assertThrows(ApiException.class,
                ()->accountService.depositAction(action, money, accountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void depositAction_whenInvokeWithAccountNotFound_thenThrowEntityNotFoundException() {
        String action = "put";
        float money = 12.0F;
        int accountNumber = 4567;
        String pin = "1234";
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                ()->accountService.depositAction(action, money, accountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void depositAction_whenInvokeWithIncorrectPIN_thenThrowAccountValidationException() {
        String action = "put";
        float money = 12.0F;
        int accountNumber = 4567;
        String pin = "1236"; //correct pin=1234
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));

        assertThrows(AccountValidationException.class,
                ()->accountService.depositAction(action, money, accountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void depositAction_whenInvokeWithNoMoneyOnAccount_thenThrowAccountValidationException() {
        String action = "withdraw";
        float money = 120.0F;
        int accountNumber = 4567;
        String pin = "1234";
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));

        assertThrows(AccountValidationException.class,
                ()->accountService.depositAction(action, money, accountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void transferMoney_whenInvoke_thenReturnListAccountDto() {
        int recipientAccountNumber = 1234;
        float money = 12.0F;
        int senderAccountNumber = 4567;
        String pin = "1234";
        Account sender = account;
        sender.setMoney(BigDecimal.valueOf(120.0F));
        Account recipient = Account.builder()
                .name("Username")
                .pin(pin)
                .accountNumber(recipientAccountNumber)
                .money(BigDecimal.ZERO)
                .build();
        when(accountRepository.findByNumber(recipientAccountNumber)).thenReturn(Optional.of(recipient));
        when(accountRepository.findByNumber(senderAccountNumber)).thenReturn(Optional.of(sender));
        when(accountMapper.accountToDto(any())).thenReturn(accountDto);

        List<AccountDto> accountsDtoActual = accountService.transferMoney(recipientAccountNumber, money, senderAccountNumber, pin);

        assertFalse(accountsDtoActual.isEmpty());
        assertEquals(2,accountsDtoActual.size());
        verify(accountRepository, atLeast(2)).update(any());
    }

    @Test
    void transferMoney_whenInvokeWithIncorrectPIN_thenThrowAccountValidationException() {
        int recipientAccountNumber = 1234;
        float money = 12.0F;
        int senderAccountNumber = 4567;
        String pin = "1237";
        Account sender = account;
        Account recipient = Account.builder()
                .name("Username")
                .pin(pin)
                .accountNumber(recipientAccountNumber)
                .money(BigDecimal.ZERO)
                .build();
        when(accountRepository.findByNumber(recipientAccountNumber)).thenReturn(Optional.of(recipient));
        when(accountRepository.findByNumber(senderAccountNumber)).thenReturn(Optional.of(sender));

        assertThrows(AccountValidationException.class,
                ()-> accountService.transferMoney(recipientAccountNumber, money, senderAccountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void transferMoney_whenInvokeAccountNotFound_thenThrowEntityNotFoundException() {
        int recipientAccountNumber = 1234;
        float money = 12.0F;
        int senderAccountNumber = 4567;
        String pin = "1237";
        Account sender = account;
        when(accountRepository.findByNumber(recipientAccountNumber)).thenReturn(Optional.empty());
        when(accountRepository.findByNumber(senderAccountNumber)).thenReturn(Optional.of(sender));

        assertThrows(EntityNotFoundException.class,
                ()-> accountService.transferMoney(recipientAccountNumber, money, senderAccountNumber, pin));

        verify(accountRepository, never()).update(any());
    }

    @Test
    void getByNumber_whenInvoke_thenReturnAccountDto() {
        int accountNumber = 1;

        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountMapper.accountToDto(account)).thenReturn(accountDto);

        AccountDto accountDtoActual = accountService.getByNumber(accountNumber);

        assertEquals(accountDtoActual, accountDto);
        verify(accountRepository).findByNumber(accountNumber);
    }

    @Test
    void getByNumber_whenRepoReturnEmptyOptional_thenThrow() {
        int accountNumber = 1;

        when(accountRepository.findByNumber(accountNumber)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                ()-> accountService.getByNumber(accountNumber));

        verify(accountMapper, never()).accountToDto(any());
    }
}
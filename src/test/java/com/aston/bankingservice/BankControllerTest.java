package com.aston.bankingservice;

import com.aston.bankingservice.model.Account;
import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private BankController bankController;

    private AccountNewDto accountNewDto;
    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void beforeEach() {
        account = Account.builder()
                .name("Username")
                .pin("1234")
                .accountNumber(3456)
                .money(BigDecimal.ZERO)
                .build();

        accountNewDto = AccountNewDto.builder()
                .name("Username")
                .pin("1234").build();

        accountDto = AccountDto.builder()
                .name("Username")
                .accountNumber(3456)
                .money(BigDecimal.ZERO).build();
    }


    @Test
    void createAccount_whenInvoke_thenRespStatusCreatedWithAccountDtoInBody() {
        when(accountService.create(accountNewDto)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = bankController.createAccount(accountNewDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(accountDto, response.getBody());
    }

    @Test
    void getAccount_whenInvoked_thenResponseStatusOKWithAccountDtoInBody() {
        when(accountService.getByNumber(3456)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = bankController.getAccount(3456);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountDto, response.getBody());
    }

    @Test
    void getAllAccounts_whenInvoked_thenResponseStatusOKWithListAccountDtoInBody() {
        List<AccountDto> expected = List.of(accountDto);
        when(accountService.getAll()).thenReturn(expected);

        ResponseEntity<List<AccountDto>> response = bankController.getAllAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void depositAction_whenInvoked_thenResponseStatusOKWithAccountDtoInBody() {
        String action = "put";
        float money = 0.0F;
        int accountNumber = 1234;
        String pin = "1234";
        when(accountService.depositAction(action, money, accountNumber, pin)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = bankController.depositAction(action, money, accountNumber, pin);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountDto, response.getBody());
    }

    @Test
    void transferMoney_whenInvoked_thenResponseStatusOKWithAccountDtoInBody() {
        List<AccountDto> expected = List.of(accountDto);
        float money = 0.0F;
        int accountNumber1 = 1234;
        int accountNumber2 = 1235;
        String pin = "1234";

        when(accountService.transferMoney(accountNumber1, money, accountNumber2, pin))
                .thenReturn(expected);

        ResponseEntity<List<AccountDto>> response =
                bankController.transferMoney(accountNumber1, money, accountNumber2, pin);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }
}
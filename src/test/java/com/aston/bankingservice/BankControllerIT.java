package com.aston.bankingservice;

import com.aston.bankingservice.exceptions.AccountValidationException;
import com.aston.bankingservice.exceptions.EntityNotFoundException;
import com.aston.bankingservice.model.Account;
import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BankController.class)
public class BankControllerIT {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;
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

    @SneakyThrows
    @Test
    void createAccount_whenInvoke_thenInvokeAccountServiceAndStatusCreated() {
        when(accountService.create(accountNewDto)).thenReturn(accountDto);

        String result = mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(accountNewDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(accountDto), result);
        verify(accountService).create(accountNewDto);
    }

    @SneakyThrows
    @Test
    void createAccount_whenInvokeWithoutBody_thenStatusBadRequest() {
        mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createAccount_whenInvokeWithInvalidBody_thenStatusBadRequest() {
        when(accountService.create(accountNewDto)).thenThrow(AccountValidationException.class);
        mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(accountNewDto)))
                .andExpect(status().isBadRequest());
        verify(accountService).create(accountNewDto);
    }

    @SneakyThrows
    @Test
    void getAccount_whenInvoke_thenInvokeAccountServiceAndStatusOk() {
        int accountNumber = 1;
        when(accountService.getByNumber(accountNumber)).thenReturn(accountDto);

        String result = mockMvc.perform(get("/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(accountDto), result);
        verify(accountService).getByNumber(accountNumber);
    }

    @SneakyThrows
    @Test
    void getAccount_whenServiceNotFound_thenStatusBadRequest() {
        int accountNumber = 1;
        when(accountService.getByNumber(accountNumber)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json"))
                .andExpect(status().isNotFound());

        verify(accountService).getByNumber(accountNumber);
    }

    @SneakyThrows
    @Test
    void getAllAccounts_whenInvoke_thenInvokeAccountServiceAndStatusOk() {
        List<AccountDto> expected = List.of(accountDto);
        when(accountService.getAll()).thenReturn(expected);

        String result = mockMvc.perform(get("/accounts/all")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expected), result);
        verify(accountService).getAll();
    }

    @SneakyThrows
    @Test
    void depositAction_whenInvoke_thenInvokeAccountServiceAndStatusOk() {
        String action = "put";
        float money = 0.0F;
        int accountNumber = 1234;
        String pin = "1234";
        when(accountService.depositAction(action, money, accountNumber, pin))
                .thenReturn(accountDto);

        String result = mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .param("action", action)
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", accountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(accountDto), result);
        verify(accountService).depositAction(action, money, accountNumber, pin);
    }

    @SneakyThrows
    @Test
    void depositAction_whenInvokeWithoutParams_thenNotInvokeAccountServiceAndStatusBadRequest() {
        int accountNumber = 1234;
        String pin = "1234";

        mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .header("X-user-account-number", accountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).depositAction(anyString(), anyFloat(), anyInt(), anyString());
    }

    @SneakyThrows
    @Test
    void depositAction_whenInvokeWithoutHeaders_thenNotInvokeAccountServiceAndStatusBadRequest() {
        String action = "put";
        float money = 0.0F;

        mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .param("action", action)
                        .param("money", String.valueOf(money)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).depositAction(anyString(), anyFloat(), anyInt(), anyString());
    }

    @SneakyThrows
    @Test
    void depositAction_whenServiceNotFountAccount_thenStatusNotFound() {
        String action = "put";
        float money = 0.0F;
        int accountNumber = 1234;
        String pin = "1234";
        when(accountService.depositAction(action, money, accountNumber, pin))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .param("action", action)
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", accountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isNotFound());

        verify(accountService).depositAction(action, money, accountNumber, pin);
    }

    @SneakyThrows
    @Test
    void depositAction_whenInvokeWithIncorrectPin_thenStatusBadRequest() {
        String action = "put";
        float money = 0.0F;
        int accountNumber = 1234;
        String pin = "1234";
        when(accountService.depositAction(action, money, accountNumber, pin))
                .thenThrow(AccountValidationException.class);

        mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .param("action", action)
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", accountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isBadRequest());

        verify(accountService).depositAction(action, money, accountNumber, pin);
    }

    @SneakyThrows
    @Test
    void depositAction_whenInvokeWithNegativeMoney_thenStatusBadRequest() {
        String action = "put";
        float money = -10.0F;
        int accountNumber = 1234;
        String pin = "1234";
        when(accountService.depositAction(action, money, accountNumber, pin))
                .thenThrow(AccountValidationException.class);

        mockMvc.perform(patch("/accounts/deposit/")
                        .contentType("application/json")
                        .param("action", action)
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", accountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isBadRequest());

        verify(accountService).depositAction(action, money, accountNumber, pin);
    }

    @SneakyThrows
    @Test
    void transferMoney_whenInvoke_thenInvokeAccountServiceAndStatusOk() {
        List<AccountDto> expected = List.of(accountDto);
        int accountNumber = 1234;
        float money = 0.0F;
        int userAccountNumber = 3456;
        String pin = "1234";
        when(accountService.transferMoney(accountNumber, money, userAccountNumber, pin))
                .thenReturn(expected);

        String result = mockMvc.perform(patch("/accounts/transfer/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", userAccountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expected), result);
        verify(accountService).transferMoney(accountNumber, money, userAccountNumber, pin);
    }

    @SneakyThrows
    @Test
    void transferMoney_whenServiceNotFound_thenStatusNotFound() {
        int accountNumber = 1234;
        float money = 0.0F;
        int userAccountNumber = 3456;
        String pin = "1234";
        when(accountService.transferMoney(accountNumber, money, userAccountNumber, pin))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(patch("/accounts/transfer/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", userAccountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isNotFound());

        verify(accountService).transferMoney(accountNumber, money, userAccountNumber, pin);
    }

    @SneakyThrows
    @Test
    void transferMoney_whenServiceReceivedIncorrectPin_thenStatusBadRequest() {
        int accountNumber = 1234;
        float money = 0.0F;
        int userAccountNumber = 3456;
        String pin = "1234";
        when(accountService.transferMoney(accountNumber, money, userAccountNumber, pin))
                .thenThrow(AccountValidationException.class);

        mockMvc.perform(patch("/accounts/transfer/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .param("money", String.valueOf(money))
                        .header("X-user-account-number", userAccountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isBadRequest());

        verify(accountService).transferMoney(accountNumber, money, userAccountNumber, pin);
    }

    @SneakyThrows
    @Test
    void transferMoney_whenInvokeWithoutHeaders_thenNotInvokeAccountServiceAndStatusBadRequest() {
        int accountNumber = 1234;
        float money = 0.0F;

        mockMvc.perform(patch("/accounts/transfer/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .param("money", String.valueOf(money)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).transferMoney(anyInt(), anyFloat(), anyInt(), anyString());
    }

    @SneakyThrows
    @Test
    void transferMoney_whenInvokeWithoutParams_thenNotInvokeAccountServiceAndStatusBadRequest() {
        int accountNumber = 1234;
        int userAccountNumber = 3456;
        String pin = "1234";

        mockMvc.perform(patch("/accounts/transfer/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("X-user-account-number", userAccountNumber)
                        .header("X-user-pin-code", pin))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).transferMoney(anyInt(), anyFloat(), anyInt(), anyString());
    }
}

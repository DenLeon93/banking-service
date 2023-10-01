package com.aston.bankingservice.service;

import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;

import java.util.List;

public interface AccountService {
    AccountDto create(AccountNewDto accountNewDto);

    AccountDto getByNumber(int accountNumber);

    List<AccountDto> getAll();

    AccountDto depositAction(String action, float money, int accountNumber, String pin);

    List<AccountDto> transferMoney(int accountNumber, float money, int userAccountNumber, String pin);
}

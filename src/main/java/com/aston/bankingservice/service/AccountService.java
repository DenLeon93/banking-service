package com.aston.bankingservice.service;

import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;

public interface AccountService {
    AccountDto create(AccountNewDto accountNewDto);
}

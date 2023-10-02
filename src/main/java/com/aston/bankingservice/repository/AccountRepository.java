package com.aston.bankingservice.repository;

import com.aston.bankingservice.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByNumber(int accountNumber);

    List<Account> getAll();

    Account update(Account account);

    void delete(Account account);
}

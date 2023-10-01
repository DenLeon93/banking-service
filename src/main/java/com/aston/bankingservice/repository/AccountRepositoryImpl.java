package com.aston.bankingservice.repository;

import com.aston.bankingservice.model.Account;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AccountRepositoryImpl implements AccountRepository {

    private final Map<Integer, Account> storage = new HashMap<>();


    @Override
    public Account save(Account account) {
        Random random = new Random();
        boolean accNumContains = true;

        while (accNumContains) {
            int accountNumber = random.nextInt(9998) + 1;
            accNumContains = storage.containsKey(accountNumber);
            account.setAccountNumber(accountNumber);
        }

        storage.put(account.getAccountNumber(), account);
        return account;
    }

    @Override
    public Optional<Account> findByNumber(int accountNumber) {
        if (storage.containsKey(accountNumber)){
            return Optional.of(storage.get(accountNumber));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Account> getAll() {
        return (List<Account>) storage.values();
    }

    @Override
    public Account update(Account account) {
        storage.replace(account.getAccountNumber(), account);
        return account;
    }
}

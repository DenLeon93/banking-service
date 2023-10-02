package com.aston.bankingservice.model;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto accountToDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountDto accountDto = AccountDto.builder()
                .name(account.getName())
                .money(account.getMoney())
                .accountNumber(account.getAccountNumber())
                .build();
        return accountDto;
    }

    public Account accountFromNewDto(AccountNewDto accountNewDto) {
        if (accountNewDto == null) {
            return null;
        }

        Account account = Account.builder()
                .name(accountNewDto.getName())
                .pin(accountNewDto.getPin())
                .build();
        return account;
    }

    public void updateFromUpdateDto(Account account, AccountUpdateDto accountUpdateDto) {

        if ( accountUpdateDto == null ) {
            return;
        }

        if ( accountUpdateDto.getName() != null ) {
            account.setName( accountUpdateDto.getName() );
        }
        if ( accountUpdateDto.getPin() != null ) {
            account.setPin( accountUpdateDto.getPin() );
        }
    }
}

package com.aston.bankingservice.exceptions;

public class AccountValidationException  extends RuntimeException{
    public AccountValidationException(String message) {
        super(message);
    }
}

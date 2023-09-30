package com.aston.bankingservice.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Account {

    private String name;

    private String pin;

    private int accountNumber;

    private BigDecimal money;
}

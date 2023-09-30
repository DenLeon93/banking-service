package com.aston.bankingservice.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountDto {

    private String name;

    private BigDecimal money;
}

package com.aston.bankingservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountNewDto {

    @NotBlank(message = "Field name may not be null and empty.")
    @Size(max = 50,
            message = "Field name size must be max 50 characters.")
    private String name;

    @NotBlank(message = "Field pin may not be null and empty.")
    private String pin;
}

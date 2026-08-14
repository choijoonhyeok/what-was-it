package com.whatwasit.backend.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record TransactionDTO (
        @NotBlank
        String merchantName,

                @NotNull
                @Positive
                        Long amount,

                @NotNull
                        LocalDateTime transactionAt
){

}




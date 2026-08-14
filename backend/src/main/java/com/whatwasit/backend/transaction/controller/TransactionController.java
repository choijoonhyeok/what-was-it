package com.whatwasit.backend.transaction.controller;

import com.whatwasit.backend.transaction.dto.TransactionDTO;
import com.whatwasit.backend.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Long> createTransaction(
            @Valid @RequestBody TransactionDTO dto
    ) {
        Long transactionId = transactionService.createTransaction(dto);

        return ResponseEntity.ok(transactionId);
    }
}
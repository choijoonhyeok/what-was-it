package com.whatwasit.backend.transaction.controller;

import com.whatwasit.backend.transaction.dto.TransactionDTO;
import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import com.whatwasit.backend.transaction.service.TransactionService;
import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
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

    @GetMapping("/{transactionId}")
    public PaymentTransactionEntity getTransaction(
            @PathVariable Long transactionId
    ) {
        return transactionService.getTransaction(transactionId);
    }

    @GetMapping("/{transactionId}/analyze")
    public AiResponseDTO analyzeTransaction(
            @PathVariable Long transactionId
    ){
        return transactionService.analyzeTransaction(transactionId);
    }

}
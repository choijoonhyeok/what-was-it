package com.whatwasit.backend.transaction.service;

import com.whatwasit.backend.transaction.dto.TransactionDTO;
import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import com.whatwasit.backend.transaction.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public Long createTransaction(TransactionDTO dto) {

        PaymentTransactionEntity entity = PaymentTransactionEntity.builder()
                .merchantName(dto.merchantName())
                .amount(dto.amount())
                .transactionAt(dto.transactionAt())
                .build();

        PaymentTransactionEntity savedEntity = paymentTransactionRepository.save(entity);

    return savedEntity.getTransactionId();
    }
}

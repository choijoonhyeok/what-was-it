package com.whatwasit.backend.transaction.repository;

import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {


}

package com.whatwasit.backend.transaction.service;

import com.whatwasit.backend.ai.AiClient;
import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
import com.whatwasit.backend.analysis.candidate.dto.CandidateDTO;
import com.whatwasit.backend.analysis.candidate.entity.AnalysisCandidateEntity;
import com.whatwasit.backend.analysis.candidate.repository.AnalysisCandidateRepository;
import com.whatwasit.backend.analysis.entity.AnalysisEntity;
import com.whatwasit.backend.analysis.repository.AnalysisRepository;
import com.whatwasit.backend.transaction.dto.TransactionDTO;
import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import com.whatwasit.backend.transaction.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AiClient aiClient;
    private final AnalysisRepository analysisRepository;
    private final AnalysisCandidateRepository analysisCandidateRepository;

    public Long createTransaction(TransactionDTO dto) {

        PaymentTransactionEntity entity = PaymentTransactionEntity.builder()
                .merchantName(dto.merchantName())
                .amount(dto.amount())
                .transactionAt(dto.transactionAt())
                .build();

        PaymentTransactionEntity savedEntity = paymentTransactionRepository.save(entity);

    return savedEntity.getTransactionId();
    }

    public PaymentTransactionEntity getTransaction(Long transactionId) {

        return paymentTransactionRepository.findById(transactionId).orElseThrow(()->new IllegalArgumentException("거래내역을 찾을 수 없습니다."));


    }

    public AiResponseDTO analyzeTransaction(Long transactionId) {

        PaymentTransactionEntity transaction =
                paymentTransactionRepository.findById(transactionId)
                        .orElseThrow(()->
                                new IllegalArgumentException("거래내역을 찾을 수 없습니다.")

                        );

        AiResponseDTO aiResponse = aiClient.analyze(transaction);

        AnalysisEntity analysis = AnalysisEntity.builder()
                .transaction(transaction)
                .status("COMPLETED")
                .summary(aiResponse.getSummary())
                .confidence(aiResponse.getConfidence())
                .modelName("what-was-it-ai-v1")
                .build();

        analysisRepository.save(analysis);

        for (CandidateDTO candidate : aiResponse.getCandidates()) {

            AnalysisCandidateEntity candidateEntity =
                    AnalysisCandidateEntity.builder()
                            .analysis(analysis)
                            .candidateName(candidate.getName())
                            .description(candidate.getDescription())
                            .score(candidate.getScore())
                            .reason(candidate.getReason())
                            .build();

            analysisCandidateRepository.save(candidateEntity);
        }




        return aiResponse;

    }



}

package com.whatwasit.backend.ai;

import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
import com.whatwasit.backend.analysis.candidate.dto.CandidateDTO;
import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiClient {

    public AiResponseDTO analyze(PaymentTransactionEntity transaction) {
        CandidateDTO candidate1 = new CandidateDTO(
                transaction.getMerchantName(),
                "가맹점명과 일치하는 결제 후보",
                0.92, "거래 내역의 가맹점명과 후보명이 일치합니다." );

        CandidateDTO candidate2 = new CandidateDTO(
                "온라인 결제", "온라인에서 발생한 결제",
                0.61, "가맹점명이 온라인 결제대행사와 유사합니다." );
        return new AiResponseDTO(
                "온라인 결제로 추정됩니다.",
                0.92,
                List.of(candidate1, candidate2)
        );
    }
}

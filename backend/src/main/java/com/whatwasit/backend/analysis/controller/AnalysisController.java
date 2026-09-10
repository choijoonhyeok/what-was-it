
package com.whatwasit.backend.analysis.controller;

import com.whatwasit.backend.ai.AiClient;
import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class AnalysisController {

    private final AiClient aiClient;

    @PostMapping
    public AiResponseDTO analyze(
            @RequestParam String merchantName,
            @RequestParam Long amount,
            @RequestParam String transactionAt
    ) {

        System.out.println("===== ANALYSIS CONTROLLER START =====");
        System.out.println("merchantName: " + merchantName);
        System.out.println("amount: " + amount);
        System.out.println("transactionAt: " + transactionAt);

        return aiClient.analyze(
                merchantName,
                amount,
                transactionAt
        );
    }
}


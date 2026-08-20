package com.whatwasit.backend.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
import com.whatwasit.backend.transaction.entity.PaymentTransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public AiResponseDTO analyze(PaymentTransactionEntity transaction) {

        String prompt = """ 
                다음 결제 내역을 분석해주세요.
                
                가맹점명: %s 
                결제금액: %s원 
                결제시간: %s 
                
                반드시 아래 JSON 형식으로만 응답해주세요. 
                { 
                "summary": "결제 내역에 대한 요약", 
                "confidence": 0.0, 
                "candidates": [ 
                { 
                "name": "후보 가맹점명", 
                "description": "후보에 대한 설명", 
                "score": 0.0, 
                "reason": "이 후보라고 판단한 이유" 
                } 
                ] 
                } 
                candidates에는 가능성이 높은 후보를 3개 만들어주세요. 
                score와 confidence는 0.0부터 1.0 사이의 숫자로 작성해주세요. 
                JSON 이외의 설명은 절대 포함하지 마세요. 
                """.formatted(
                        transaction.getMerchantName(),
                        transaction.getAmount(),
                        transaction.getTransactionAt()
        );


        ResponseCreateParams params = ResponseCreateParams
                .builder().model(ChatModel.GPT_5_2)
                .input(prompt)
                .build();

        Response response = openAIClient.responses().create(params);

        System.out.println("===== OpenAI 응답 =====");
        response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(outputText -> System.out.println(outputText.text()));


        String jsonResponse = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("OpenAI 응답이 없습니다.")
                );

        System.out.println("===== OpenAI 응답 =====");
        System.out.println(jsonResponse);

        try {
            return objectMapper.readValue(
                    jsonResponse,
                    AiResponseDTO.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "OpenAI 응답 JSON 파싱에 실패했습니다.",
                    e
            );
        }



    }
}

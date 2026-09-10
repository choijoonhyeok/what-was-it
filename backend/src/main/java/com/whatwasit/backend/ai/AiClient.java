package com.whatwasit.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.whatwasit.backend.ai.PythonAiClient;
import com.whatwasit.backend.analysis.candidate.dto.AiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final PythonAiClient pythonAiClient;

    public AiResponseDTO analyze(
            String merchantName,
            Long amount,
            String transactionAt
    ) {

        // 1. Python AI 서버에서 후보 + 검색 근거 가져오기
        var analyzeResponse = pythonAiClient.analyze(
                merchantName
        );

        System.out.println("===== Python AI 응답 =====");

        for (var candidate : analyzeResponse.candidates()) {
            System.out.println("후보: " + candidate.candidate().name());
            System.out.println("유사도: " + candidate.candidate().score());
            System.out.println(candidate.ragContext());
        }

        // 2. GPT에게 전달할 후보 및 근거 생성
        StringBuilder candidateContext = new StringBuilder();

        for (var candidate : analyzeResponse.candidates()) {

            candidateContext.append("""
                    후보 가맹점명: %s
                    카테고리: %s
                    설명: %s
                    SBERT 유사도: %s

                    외부 검색 근거:
                    %s

                    --------------------
                    """.formatted(
                    candidate.candidate().name(),
                    candidate.candidate().category(),
                    candidate.candidate().description(),
                    candidate.candidate().score(),
                    candidate.ragContext()
            ));
        }

        // 3. GPT 최종 판단
        String prompt = """
                다음 결제 내역의 실제 가맹점이 무엇인지 판단해주세요.

                [결제 내역]
                가맹점명: %s
                결제금액: %s원
                결제시간: %s

                [후보 가맹점 및 외부 검색 근거]
                %s

                위 후보들은 Sentence-BERT를 이용해 유사도가 높은 순서로 선정된 후보입니다.
                외부 검색 근거를 함께 검토하여 가장 가능성이 높은 후보를 판단해주세요.

                반드시 아래 JSON 형식으로만 응답해주세요.

                {
                  "summary": "결제 내역에 대한 최종 판단 요약",
                  "confidence": 0.0,
                  "candidates": [
                    {
                      "name": "후보 가맹점명",
                      "description": "후보에 대한 설명",
                      "score": 0.0,
                      "reason": "검색 근거와 가맹점명을 바탕으로 판단한 이유"
                    }
                  ]
                }

                candidates에는 제공된 후보 중 가능성이 높은 후보를 최대 3개까지 작성해주세요.
                새로운 후보를 임의로 생성하지 마세요.
                score와 confidence는 0.0부터 1.0 사이의 숫자로 작성해주세요.
                JSON 이외의 설명은 절대 포함하지 마세요.
                """.formatted(
                merchantName,
                amount,
                transactionAt,
                candidateContext
        );

        ResponseCreateParams params = ResponseCreateParams
                .builder()
                .model(ChatModel.GPT_5_2)
                .input(prompt)
                .build();

        Response response = openAIClient.responses().create(params);

        String jsonResponse = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("OpenAI 응답이 없습니다.")
                );

        System.out.println("===== OpenAI 최종 응답 =====");
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

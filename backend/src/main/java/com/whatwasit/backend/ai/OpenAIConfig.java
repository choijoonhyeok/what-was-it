package com.whatwasit.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient(){

        //OPENAI_API_KEY 환경변수를 읽어준다.
        return OpenAIOkHttpClient.fromEnv();

    }

    @Bean public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

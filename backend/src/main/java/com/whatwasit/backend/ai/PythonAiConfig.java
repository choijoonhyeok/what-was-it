package com.whatwasit.backend.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PythonAiConfig {

    @Bean
    public RestClient pythonRestClient(){
        return RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

    }
}

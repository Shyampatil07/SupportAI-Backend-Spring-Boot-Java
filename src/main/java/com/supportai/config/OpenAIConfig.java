//package com.supportai.config;
//
//import com.openai.client.OpenAIClient;
//import com.openai.client.okhttp.OpenAIOkHttpClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class OpenAIConfig {
//
//    private final AiProperties aiProperties;
//
//    @Bean
//    public OpenAIClient openAIClient() {
//
//        return OpenAIOkHttpClient.builder()
////                .apiKey(aiProperties.getApiKey())
//                .build();
//    }
//}
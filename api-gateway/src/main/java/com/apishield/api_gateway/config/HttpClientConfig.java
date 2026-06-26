package com.apishield.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class HttpClientConfig {
    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .defaultHeader("X-Internal-Source","api-shield-gateway")
                .build();
    }
}

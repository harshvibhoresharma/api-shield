package com.example.config_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RateLimitRuleResponse {
    private String algorithm;
    private int limitValue;
    private int windowSeconds;
    private String tier;
    private String endpoint;
}
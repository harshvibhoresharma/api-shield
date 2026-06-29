package com.apishield.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO matching your DB schema: rate_limit_rules table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitRule {
    private String algorithm;   // "token_bucket" per diagram
    private int limitValue;
    private int windowSeconds;
    private String tier;        // free / pro
    private String endpoint;    // pattern
}
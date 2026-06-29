package com.apishield.api_gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackendUrlResponse {
    private String backendUrl;
    private int cacheTtlSeconds;
}
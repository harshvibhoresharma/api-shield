package com.example.config_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// dto/BackendUrlResponse.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendUrlResponse {
    private String backendUrl;
    private int cacheTtlSeconds;
}
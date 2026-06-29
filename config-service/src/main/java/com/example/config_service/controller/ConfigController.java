package com.example.config_service.controller;

import com.example.config_service.dto.BackendUrlResponse;
import com.example.config_service.dto.RateLimitRuleResponse;
import com.example.config_service.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/config")
    public ResponseEntity<BackendUrlResponse> getBackendUrl(
            @RequestParam("apiKey") String apiKey,
            @RequestParam("apiName") String apiName) {
        return configService.getBackendUrl(apiKey, apiName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rules")
    public ResponseEntity<RateLimitRuleResponse> getRateLimitRule(
            @RequestParam("apiKey") String apiKey,
            @RequestParam("endpoint") String endpoint) {
        return configService.getRateLimitRule(apiKey, endpoint)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateApiKey(@RequestParam("apiKey") String apiKey) {
        return ResponseEntity.ok(configService.isValidApiKey(apiKey));
    }
}
package com.example.config_service.service;

import com.example.config_service.dto.BackendUrlResponse;
import com.example.config_service.dto.RateLimitRuleResponse;
import com.example.config_service.repository.ConsumerRepository;
import com.example.config_service.repository.RateLimitRuleRepository;
import com.example.config_service.repository.RegisteredApiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {

    private final ConsumerRepository consumerRepository;
    private final RegisteredApiRepository registeredApiRepository;
    private final RateLimitRuleRepository rateLimitRuleRepository;

    public Optional<BackendUrlResponse> getBackendUrl(String apiKey, String apiName) {
        return consumerRepository.findByApiKey(apiKey)
                .flatMap(consumer -> registeredApiRepository
                        .findByOwnerIdAndName(consumer.getId(), apiName))
                .map(api -> new BackendUrlResponse(api.getBackendUrl(), api.getCacheTtlSeconds()));
    }

    public Optional<RateLimitRuleResponse> getRateLimitRule(String apiKey, String endpoint) {
        return consumerRepository.findByApiKey(apiKey)
                .flatMap(consumer -> registeredApiRepository
                        .findByOwnerId(consumer.getId())
                        .stream()
                        .findFirst()
                        .flatMap(api -> rateLimitRuleRepository
                                .findByApiIdAndTierAndEndpoint(api.getId(), consumer.getTier(), endpoint)
                                .map(rule -> new RateLimitRuleResponse(
                                        rule.getAlgorithm(),
                                        rule.getLimitValue(),
                                        rule.getWindowSeconds(),
                                        rule.getTier(),
                                        rule.getEndpoint()
                                ))));
    }

    public boolean isValidApiKey(String apiKey) {
        return consumerRepository.findByApiKey(apiKey).isPresent();
    }
}
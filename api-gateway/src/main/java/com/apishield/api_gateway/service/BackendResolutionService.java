package com.apishield.api_gateway.service;

import com.apishield.api_gateway.cache.ApiCacheService;
import com.apishield.api_gateway.client.ConfigServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackendResolutionService {

    private final ApiCacheService apiCacheService;
    private final ConfigServiceClient configServiceClient;

    public String resolve(String apiKey, String apiName) {
        String cached = apiCacheService.getBackendUrl(apiKey);
        if (cached != null) {
            log.debug("Cache HIT for apiKey: {}", apiKey);
            return cached;
        }
        log.debug("Cache MISS for apiKey: {}, calling config-service", apiKey);
        String url = configServiceClient.fetchBackendUrl(apiKey, apiName);
        if (url != null) {
            apiCacheService.cacheBackendUrl(apiKey, url);
        }
        return url;
    }
}
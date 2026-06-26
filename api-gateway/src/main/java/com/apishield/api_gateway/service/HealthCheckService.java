package com.apishield.api_gateway.service;

import com.apishield.api_gateway.cache.ApiCacheService;
import com.apishield.api_gateway.client.BackendHealthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {
    private final ApiCacheService apiCacheService;
    private final BackendHealthClient backendHealthClient;
    public boolean isAlive(String backendUrl){
        String cached = apiCacheService.getHealthStatus(backendUrl);
        if(cached!=null){
            return "UP".equals(cached);
        }
        boolean alive = backendHealthClient.pingHealthEndpoint(backendUrl);
        apiCacheService.cacheHealthStatus(backendUrl,alive);
        log.info("Health check for {}:{}",backendUrl,alive ? "UP":"DOWN");
        return alive;
    }
}


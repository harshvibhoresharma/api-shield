package com.apishield.api_gateway.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCacheService {
    private final RedisTemplate<String,String> redisTemplate;
    private static final String URL_PREFIX="api:url:";
    private static final String HEALTH_PREFIX = "api:health:";
    private static final long URL_TTL = 300L;
    private static final long HEALTH_TTL=300L;
    public String getBackendUrl(String apiKey){
        return  redisTemplate.opsForValue().get(URL_PREFIX+apiKey);
    }
    public void cacheBackendUrl(String apiKey,String url){
        redisTemplate.opsForValue().set(URL_PREFIX+apiKey,url,URL_TTL, TimeUnit.SECONDS);
        log.debug("cached backend url for {}:{}",apiKey,url);
    }
    public void evictBackendUrl(String apiKey){
        redisTemplate.delete(URL_PREFIX+apiKey);
    }
    public String getHealthStatus(String backendUrl){
        return redisTemplate.opsForValue().get(HEALTH_PREFIX+backendUrl);
    }
    public void cacheHealthStatus(String backendUrl,boolean isUp){
        redisTemplate.opsForValue().set(
          HEALTH_PREFIX+backendUrl,
          isUp ? "UP":"DOWN",
          HEALTH_TTL,
          TimeUnit.SECONDS
        );
    }
}

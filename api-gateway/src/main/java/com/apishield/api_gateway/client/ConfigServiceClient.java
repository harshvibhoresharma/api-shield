package com.apishield.api_gateway.client;

import com.apishield.api_gateway.dto.BackendUrlResponse;
import com.apishield.api_gateway.dto.RateLimitRule;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ConfigServiceClient {

    private final String configServiceUrl;
    private final RestClient restClient;

    public ConfigServiceClient(RestClient restClient,
                               @Value("${config.service.url}") String configServiceUrl) {
        this.restClient = restClient;
        this.configServiceUrl = configServiceUrl;
    }

    public String fetchBackendUrl(String apiKey, String apiName) {
        try {
            BackendUrlResponse backendUrlResponse = restClient.get()
                    .uri(configServiceUrl + "/apis/config?apiKey={apiKey}&apiName={apiName}",
                            apiKey, apiName)
                    .retrieve()
                    .body(BackendUrlResponse.class);
            return backendUrlResponse != null ? backendUrlResponse.getBackendUrl() : null;
        } catch (Exception e) {
            log.error("Config service unreachable for apiKey {}: {}", apiKey, e.getMessage());
            return null;
        }
    }

    public RateLimitRule getRateLimitRule(String apiKey, String endpoint) {
        try {
            return restClient.get()
                    .uri(configServiceUrl + "/apis/rules?apiKey={apiKey}&endpoint={endpoint}",
                            apiKey, endpoint)
                    .retrieve()
                    .body(RateLimitRule.class);
        } catch (Exception e) {
            log.warn("Could not fetch rate limit rule for {}: {}", apiKey, e.getMessage());
            return null;
        }
    }
}
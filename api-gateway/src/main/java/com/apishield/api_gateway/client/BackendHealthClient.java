package com.apishield.api_gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Component
@RequiredArgsConstructor
@Slf4j
public class BackendHealthClient {
    private final RestClient restClient;
    public boolean pingHealthEndpoint(String backendUrl) {
        try {
            restClient.get()
                    .uri(backendUrl + "/")
                    .retrieve()
                    .onStatus(status -> false, (req, res) -> {}) // never throw on any status
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Failed ping health for {}: {}", backendUrl, e.getMessage());
            return false;
        }
    }
}

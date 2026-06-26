package com.apishield.api_gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigServiceClient {
    private final RestClient restClient;
    @Value("")

}

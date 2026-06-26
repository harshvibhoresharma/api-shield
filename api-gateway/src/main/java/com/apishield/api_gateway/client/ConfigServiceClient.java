package com.apishield.api_gateway.client;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Slf4j
public class ConfigServiceClient {
    private String configServiceUrl;
    private final RestClient restClient;
    public ConfigServiceClient(RestClient restClient,@Value("{config.service.url") String configServiceUrl){
        this.restClient=restClient;
        this.configServiceUrl=configServiceUrl;
    }
    public String fetchBackendUrl(String apiKey){
        try{
            return restClient.get()
                    .uri(configServiceUrl+"/apis/config?apiKey={apiKey}",apiKey)
                    .retrieve()
                    .body(String.class);
        }catch (Exception e){
            log.error("Config service unreachable for apiKey {}:{}",apiKey,e.getMessage());
            return null;
        }
    }



}

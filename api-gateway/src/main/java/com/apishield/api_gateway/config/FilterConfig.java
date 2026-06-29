package com.apishield.api_gateway.config;

import com.apishield.api_gateway.client.ConfigServiceClient;
import com.apishield.api_gateway.filter.JwtAuthFilter;
import com.apishield.api_gateway.filter.RateLimitFilter;
import com.apishield.api_gateway.filter.RoutingFilter;
import com.apishield.api_gateway.service.BackendResolutionService;
import com.apishield.api_gateway.service.HealthCheckService;
import com.apishield.api_gateway.util.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class FilterConfig {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ConfigServiceClient configServiceClient;
    private final BackendResolutionService backendResolutionService;
    private final HealthCheckService healthCheckService;
    private final RestClient restClient;

    public FilterConfig(JwtUtil jwtUtil,
                        RedisTemplate<String, String> redisTemplate,
                        ConfigServiceClient configServiceClient,
                        BackendResolutionService backendResolutionService,
                        HealthCheckService healthCheckService,
                        RestClient restClient) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.configServiceClient = configServiceClient;
        this.backendResolutionService = backendResolutionService;
        this.healthCheckService = healthCheckService;
        this.restClient = restClient;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthFilter(jwtUtil));
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateFilter() {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RateLimitFilter(redisTemplate, configServiceClient));
        bean.addUrlPatterns("/*");
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RoutingFilter> routingFilter() {
        FilterRegistrationBean<RoutingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RoutingFilter(backendResolutionService, healthCheckService, restClient));
        bean.addUrlPatterns("/*");
        bean.setOrder(3);
        return bean;
    }
}
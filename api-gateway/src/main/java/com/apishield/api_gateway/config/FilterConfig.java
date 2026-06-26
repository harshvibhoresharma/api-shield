package com.apishield.api_gateway.config;

import com.apishield.api_gateway.filter.JwtAuthFilter;
import com.apishield.api_gateway.filter.RateLimitFilter;
import com.apishield.api_gateway.filter.RoutingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final RoutingFilter routingFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(jwtAuthFilter);
        bean.addUrlPatterns("/*");
        bean.setOrder(1);  // runs first
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateFilter() {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(rateLimitFilter);
        bean.addUrlPatterns("/*");
        bean.setOrder(2);  // runs second
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RoutingFilter> routingFilter() {
        FilterRegistrationBean<RoutingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(routingFilter);
        bean.addUrlPatterns("/*");
        bean.setOrder(3);  // runs last
        return bean;
    }
}
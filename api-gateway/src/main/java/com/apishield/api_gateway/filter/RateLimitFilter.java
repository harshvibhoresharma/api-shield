package com.apishield.api_gateway.filter;

import com.apishield.api_gateway.client.ConfigServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String TOKEN_BUCKET_SCRIPT = "";
    private final RedisTemplate<String, String> redisTemplate;
    private final ConfigServiceClient configServiceClient; // Feign client to your Config Service

    // Lua script: atomic token bucket
    
    private final RedisScript<List<Long>> rateLimitScript =
            RedisScript.of(TOKEN_BUCKET_SCRIPT, (Class<List<Long>>) (Class<?>) List.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract API key from header (per your diagram)
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing API key");
            return;
        }

        // Fetch limit rule from Config Service (cached via @Cacheable there)
        RateLimitRule rule = configServiceClient.getRateLimitRule(apiKey);
        if (rule == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid API key");
            return;
        }

        // Redis key: rate_limit:{api_key}:{endpoint} (per your diagram)
        String redisKey = String.format("ratelimit:%s:%s", apiKey, path);

        long now = System.currentTimeMillis() / 1000;

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
                rateLimitScript,
                List.of(redisKey),
                String.valueOf(now),
                String.valueOf(rule.getLimitValue()),
                String.valueOf(rule.getWindowSeconds())
        );

        long allowed = result.get(0);
        long remaining = result.get(1);

        response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.addHeader("X-RateLimit-Limit", String.valueOf(rule.getLimitValue()));

        if (allowed == 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("Retry-After", String.valueOf(rule.getWindowSeconds()));
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
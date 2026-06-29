package com.apishield.api_gateway.filter;

import com.apishield.api_gateway.client.ConfigServiceClient;
import com.apishield.api_gateway.dto.RateLimitRule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_WINDOW = 60;

    private final RedisTemplate<String, String> redisTemplate;
    private final ConfigServiceClient configServiceClient;
    private final RedisScript<List<Long>> rateLimitScript;

    public RateLimitFilter(RedisTemplate<String, String> redisTemplate,
                           ConfigServiceClient configServiceClient) {
        this.redisTemplate = redisTemplate;
        this.configServiceClient = configServiceClient;
        this.rateLimitScript = RedisScript.of(TOKEN_BUCKET_SCRIPT,
                (Class<List<Long>>) (Class<?>) List.class);
    }

    private static final String TOKEN_BUCKET_SCRIPT =
            "local key = KEYS[1]\n" +
                    "local now = tonumber(ARGV[1])\n" +
                    "local limit = tonumber(ARGV[2])\n" +
                    "local window = tonumber(ARGV[3])\n" +
                    "local data = redis.call('HMGET', key, 'tokens', 'last_refill')\n" +
                    "local tokens = tonumber(data[1])\n" +
                    "local last_refill = tonumber(data[2])\n" +
                    "if tokens == nil then\n" +
                    "    tokens = limit - 1\n" +
                    "    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)\n" +
                    "    redis.call('EXPIRE', key, window)\n" +
                    "    return {1, tokens}\n" +
                    "end\n" +
                    "local elapsed = now - last_refill\n" +
                    "local refill = math.floor(elapsed * limit / window)\n" +
                    "if refill > 0 then\n" +
                    "    tokens = math.min(limit, tokens + refill)\n" +
                    "    last_refill = now\n" +
                    "end\n" +
                    "if tokens > 0 then\n" +
                    "    tokens = tokens - 1\n" +
                    "    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)\n" +
                    "    redis.call('EXPIRE', key, window)\n" +
                    "    return {1, tokens}\n" +
                    "else\n" +
                    "    return {0, 0}\n" +
                    "end";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing API key");
            return;
        }

        // use rule from config-service, fall back to defaults
        RateLimitRule rule = configServiceClient.getRateLimitRule(apiKey, path);
        if (rule == null) {
            log.debug("No rate limit rule found for {} {}, using defaults", apiKey, path);
            rule = new RateLimitRule("token_bucket", DEFAULT_LIMIT, DEFAULT_WINDOW, "free", path);
        }

        String redisKey = String.format("ratelimit:%s:%s", apiKey, path);
        long now = System.currentTimeMillis() / 1000;

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
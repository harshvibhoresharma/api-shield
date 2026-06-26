package com.apishield.api_gateway.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import com.apishield.api_gateway.service.BackendResolutionService;
import com.apishield.api_gateway.service.HealthCheckService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@RequiredArgsConstructor
@Slf4j
public class RoutingFilter extends OncePerRequestFilter {
    private final BackendResolutionService backendResolutionService;
    private  final HealthCheckService healthCheckService;
    private  final RestClient restClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        String path = request.getRequestURI();
        String backendUrl=backendResolutionService.resolve(apiKey);
        if(backendUrl==null){
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("No backend registered for this API key");
            return;
        }
        if(!healthCheckService.isAlive(backendUrl)){
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.addHeader("X-Gateway-Error","Backend server is down");
            response.getWriter().write("Service is temporarily down try again later");
            return;
        }
        forwardRequest(request,response,backendUrl,path);
    }
    private void forwardRequest(HttpServletRequest request, HttpServletResponse response,String backendUrl,String path)throws IOException{
        String targetUrl = backendUrl+path;

        try{
            byte[] body = new byte[0];
            if (request.getContentLength() > 0) {
                body = StreamUtils.copyToByteArray(request.getInputStream());
            }
            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames())
                    .forEach(h->headers.add(h,request.getHeader(h)));
            headers.add("X-Forwarded-By","api-shield-gateway");
            headers.add("X-Trace-Id", UUID.randomUUID().toString());
            ResponseEntity<byte[]> backendResponse = restClient
                    .method(HttpMethod.valueOf(request.getMethod()))
                    .uri(targetUrl)
                    .headers(h->h.addAll(headers))
                    .body(body)
                    .retrieve()
                    .toEntity(byte[].class);
            response.setStatus(backendResponse.getStatusCode().value());
            backendResponse.getHeaders()
                    .forEach((k,v)->v.forEach(val->response.addHeader(k,val)));
            if(backendResponse.getBody()!=null){
                response.getOutputStream()
                        .write(backendResponse.getBody());

            }
        }catch (HttpClientErrorException | HttpServerErrorException e){
            response.setStatus(e.getStatusCode().value());
            response.getWriter().write(e.getResponseBodyAsString());
        }catch (Exception e){
            log.error("Failed to forward to {}:{}",targetUrl,e.getMessage());
            response.setStatus(HttpStatus.BAD_GATEWAY.value());
            response.getWriter().write("Gateway error: Cant reach backend");
        }
    }
}

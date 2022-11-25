package com.example.ratelimitingdemo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
class RequestThrottleFilter implements WebFilter {

    private LoadingCache<String, Integer> requestCountsPerIpAddress;

    public RequestThrottleFilter(){
        super();
        requestCountsPerIpAddress = Caffeine.newBuilder().
                expireAfterWrite(1, TimeUnit.MINUTES).build(key -> 0);
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress){
        Integer requests;
        requests = requestCountsPerIpAddress.get(clientIpAddress);

        if (requests != null){
            int maxRequestsPerSecond = 5;
            if (requests > maxRequestsPerSecond) {
                requestCountsPerIpAddress.asMap().remove(clientIpAddress);
                requestCountsPerIpAddress.put(clientIpAddress, requests);
                return true;
            }

        } else {
            requests = 0;
        }
        requests++;
        requestCountsPerIpAddress.put(clientIpAddress, requests);
        return false;
    }

    public String getClientIP(ServerHttpRequest request) {
        String xfHeader = Objects.requireNonNull(request.getHeaders().get("X-Forwarded-For")).get(0);
        if (xfHeader == null){
            return request.getLocalAddress().toString();
        }
        return xfHeader.split(",")[0];
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain filterChain) {
        ServerHttpRequest httpServletRequest = exchange.getRequest();
        ServerHttpResponse httpServletResponse = exchange.getResponse();
        String clientIpAddress = getClientIP(httpServletRequest);

        if (isMaximumRequestsPerSecondExceeded(clientIpAddress)){
            httpServletResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return Mono.empty();
        }
        return filterChain.filter(exchange);
    }
}
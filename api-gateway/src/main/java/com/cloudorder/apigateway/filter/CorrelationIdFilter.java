package com.cloudorder.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements
        org.springframework.cloud.gateway.filter.GlobalFilter,
        Ordered {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(HEADER, correlationId)
                        .build();

        ServerWebExchange modifiedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        return chain.filter(modifiedExchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
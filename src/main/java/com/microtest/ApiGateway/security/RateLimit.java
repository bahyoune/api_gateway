package com.microtest.ApiGateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

@Configuration
public class RateLimit {

    @Bean
    public KeyResolver userAndRouteKeyResolver() {
        return exchange ->
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx ->
                                ctx.getAuthentication().getName() + ":" +
                                        exchange.getRequest().getPath().contextPath().value()
                        );
    }


}

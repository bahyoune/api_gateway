package com.microtest.ApiGateway.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;


@Configuration
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/v1/payment/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/orders/**").hasAnyRole("USER", "ADMIN")
                        .anyExchange().authenticated()
                )
                // Recommended path: use Spring's resource server to validate JWT from Authorization header
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }


    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles"); // your claim
        authoritiesConverter.setAuthorityPrefix(""); // already ROLE_

        JwtAuthenticationConverter delegate =
                new JwtAuthenticationConverter();

        delegate.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }




}

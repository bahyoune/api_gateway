package com.microtest.ApiGateway.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class SecurityBeans {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
  private   String secret ;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}

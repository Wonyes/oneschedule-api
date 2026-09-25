package com.studio.api.global.config.security;

import com.studio.api.global.auth.AuthTokenProvider;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration_time}")
    private Long expiration;

    @Value("${jwt.refresh.expiration_time}")
    private Long refreshExpiration;

    @Bean
    public AuthTokenProvider authTokenProvider() {

        SecretKey key =
                Keys.hmacShaKeyFor(secret.getBytes());

        return new AuthTokenProvider(
                key,
                expiration,
                refreshExpiration
        );
    }
}
package com.studio.api.global.auth;

import com.studio.core.global.enums.AuthRole;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

@RequiredArgsConstructor
public class AuthTokenProvider {


    private final SecretKey key;
    private final long tokenValidTime;
    private final long refreshTokenValidTime;

    public AuthToken createAccessToken(
            Long memberNo,
            String email,
            AuthRole role
    ) {
        return new AuthToken(
                memberNo,
                email,
                role,
                new Date(System.currentTimeMillis() + tokenValidTime),
                key
        );
    }

    public AuthToken createRefreshToken(Long memberId) {
        return new AuthToken(
                memberId,
                new Date(System.currentTimeMillis() + refreshTokenValidTime),
                key
        );
    }

    public LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plusSeconds(refreshTokenValidTime / 1000);
    }

    public AuthToken convertAuthToken(String token) {
        return new AuthToken(token, key);
    }
}
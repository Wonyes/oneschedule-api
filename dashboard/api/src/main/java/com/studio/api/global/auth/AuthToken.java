package com.studio.api.global.auth;

import com.studio.core.global.enums.AuthRole;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
public class AuthToken {

    private final String token;
    private final SecretKey key;

    private static final String ROLE_KEY = "role";

    public AuthToken(
            Long memberNo,
            String email,
            AuthRole role,
            Date expiry,
            SecretKey key
    ) {
        this.key = key;
        this.token = createAccessToken(
                memberNo,
                email,
                role,
                expiry
        );
    }

    public AuthToken(
            String token,
            SecretKey key
    ) {
        this.token = token;
        this.key = key;
    }

    private String createAccessToken(
            Long memberNo,
            String email,
            AuthRole role,
            Date expiry
    ) {

        return Jwts.builder()
                .subject(memberNo.toString())
                .issuedAt(new Date())
                .expiration(expiry)
                .claim("email", email)
                .claim(ROLE_KEY, role.name())
                .signWith(key)
                .compact();
    }

    public AuthToken(
            Long memberNo,
            Date expiry,
            SecretKey key
    ) {
        this.key = key;

        this.token = Jwts.builder()
                .subject(memberNo.toString())
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getToken() {
        return token;
    }

    public boolean isTokenValid() {
        try {
            getValidTokenClaims();
            return true;

        } catch (ExpiredJwtException e) {
            throw new CustomException(
                    ErrorCode.JWT_EXPIRE_TOKEN
            );

        } catch (JwtException e) {
            throw new CustomException(
                    ErrorCode.JWT_ERROR_TOKEN
            );
        }
    }

    public Claims getValidTokenClaims() {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.JWT_EXPIRE_TOKEN);

        } catch (JwtException e) {
            throw new CustomException(ErrorCode.JWT_ERROR_TOKEN);
        }
    }

    public Long getMemberNo() {
        return Long.valueOf(
                getValidTokenClaims().getSubject()
        );
    }
}
package com.studio.api.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** 인증 쿠키 설정(yml cookie.*) + 쿠키 생성. 이름·만료 시간은 여기서만 정한다 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    public static final String ACCESS = "access-token";
    public static final String REFRESH = "refresh-token";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    private boolean secure;
    private String sameSite;
    private String domain;

    public ResponseCookie access(String token) {
        return build(ACCESS, token, ACCESS_TTL);
    }

    public ResponseCookie refresh(String token) {
        return build(REFRESH, token, REFRESH_TTL);
    }

    public ResponseCookie expire(String name) {
        return build(name, "", Duration.ZERO);
    }

    private ResponseCookie build(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .domain(domain)
                .path("/")
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
    }
}

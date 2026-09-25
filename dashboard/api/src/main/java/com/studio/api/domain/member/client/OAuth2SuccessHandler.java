package com.studio.api.domain.member.client;

import com.studio.api.global.auth.AuthToken;
import com.studio.api.global.auth.AuthTokenProvider;
import com.studio.api.global.config.CookieProperties;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.entity.RefreshTokenEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.domain.member.repository.RefreshTokenRepository;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor

public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenProvider authTokenProvider;
    private final CookieProperties cookieProperties;

    @Value("${app.front-url}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Long memberNo = ((Number) principal.getAttributes().get("memberNo")).longValue();

        MemberEntity member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAIL));

        AuthToken accessToken = authTokenProvider.createAccessToken(
                member.getMemberNo(), member.getEmail(), member.getRole());
        AuthToken refreshToken = authTokenProvider.createRefreshToken(member.getMemberNo());

        refreshTokenRepository.save(
                new RefreshTokenEntity(member.getMemberNo(), refreshToken.getToken(), authTokenProvider.refreshTokenExpiresAt()));

        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieProperties.access(accessToken.getToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieProperties.refresh(refreshToken.getToken()).toString());

        getRedirectStrategy().sendRedirect(request, response, frontUrl);
    }
}

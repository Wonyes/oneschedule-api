package com.studio.api.domain.auth.service;

import com.studio.api.global.auth.AuthToken;
import com.studio.api.global.auth.AuthTokenProvider;
import com.studio.core.domain.member.dto.member.response.TokenResponse;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.entity.RefreshTokenEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.domain.member.repository.RefreshTokenRepository;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final AuthTokenProvider authTokenProvider;

    @Transactional
    public TokenResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        RefreshTokenEntity entity =
                refreshTokenRepository.findByRefreshToken(refreshToken)
                        .orElseThrow(() -> new CustomException(ErrorCode.JWT_ERROR_TOKEN));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(entity);
            throw new CustomException(ErrorCode.JWT_EXPIRE_TOKEN);
        }

        MemberEntity member =
                memberRepository.getOrThrow(entity.getMemberNo());


        AuthToken accessToken =
                authTokenProvider.createAccessToken(
                        member.getMemberNo(),
                        member.getEmail(),
                        member.getRole()
                );


        AuthToken newRefreshToken =
                authTokenProvider.createRefreshToken(
                        member.getMemberNo()
                );


        entity.updateToken(
                newRefreshToken.getToken(),
                authTokenProvider.refreshTokenExpiresAt()
        );

        return new TokenResponse(
                accessToken.getToken(),
                newRefreshToken.getToken()
        );
    }


}

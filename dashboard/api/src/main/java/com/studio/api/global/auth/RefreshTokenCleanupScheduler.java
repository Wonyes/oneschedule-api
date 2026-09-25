package com.studio.api.global.auth;

import com.studio.core.domain.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 4 * * *")   // 매일 04:00
    @Transactional
    public void cleanUp() {
        long deleted = refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) log.info("만료 리프레시 토큰 {}건 삭제", deleted);
    }
}
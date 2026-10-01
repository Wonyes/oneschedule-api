package com.studio.api.global.config;

import com.studio.core.domain.member.repository.EmailVerificationRepository;
import com.studio.core.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 쌓이기만 하는 행을 주기적으로 지운다.
 * 리프레시 토큰은 RefreshTokenCleanupScheduler가 따로 맡는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final NotificationRepository notificationRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    /** 읽은 알림은 30일, 안 읽은 알림도 90일이 지나면 볼 이유가 없다 */
    @Scheduled(cron = "0 10 4 * * *")   // 매일 04:10
    @Transactional
    public void cleanUpNotifications() {
        LocalDateTime now = LocalDateTime.now();

        int read = notificationRepository.deleteReadBefore(now.minusDays(30));
        int unread = notificationRepository.deleteCreatedBefore(now.minusDays(90));

        if (read + unread > 0) {
            log.info("알림 정리 — 읽음 {}건, 오래된 미읽음 {}건 삭제", read, unread);
        }
    }

    /** 만료된 인증 코드 행. 인증 완료분은 consumeVerified가 지우지만 미완료분이 남는다 */
    @Scheduled(cron = "0 20 4 * * *")   // 매일 04:20
    @Transactional
    public void cleanUpEmailVerifications() {
        int deleted = emailVerificationRepository.deleteExpiredBefore(LocalDateTime.now().minusDays(1));
        if (deleted > 0) log.info("만료 이메일 인증 {}건 삭제", deleted);
    }
}

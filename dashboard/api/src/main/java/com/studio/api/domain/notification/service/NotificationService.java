package com.studio.api.domain.notification.service;

import com.studio.api.global.sse.SseEmitterRegistry;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.domain.notification.entity.NotificationEntity;
import com.studio.core.domain.notification.repository.NotificationRepository;
import com.studio.core.domain.notification.response.NotificationResponse;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final SseEmitterRegistry sseEmitterRegistry;

    /** 일정과 무관한 알림 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(
            Collection<Long> receiverNos, Long senderNo, NotificationType type,
            String content, Long targetNo
    ) {
        send(receiverNos, senderNo, type, content, targetNo, null);
    }

    /** 일정 알림 — scheduleDate를 실어 클릭 시 그 날짜의 일간 뷰로 보낸다 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(
            Collection<Long> receiverNos, Long senderNo, NotificationType type,
            String content, Long targetNo, LocalDate scheduleDate
    ) {
        Set<Long> targets = new HashSet<>(receiverNos);
        if (senderNo != null) targets.remove(senderNo);
        if (targets.isEmpty()) return;

        MemberEntity sender = senderNo == null
                ? null
                : memberRepository.findById(senderNo).orElse(null);

        List<NotificationEntity> notifications = memberRepository.findAllById(targets)
                .stream()
                .map(receiver -> NotificationEntity.create(
                        receiver, sender, type, type.getTitle(), content, targetNo, scheduleDate))
                .toList();

        notificationRepository.saveAll(notifications);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        notifications.forEach(n ->
                                sseEmitterRegistry.send(
                                        n.getReceiver().getMemberNo(),
                                        "notification",
                                        new NotificationResponse(n)
                                )
                        );
                    }
                }
        );
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMine(Long memberNo, Pageable pageable) {
        return notificationRepository
                .findByReceiver(memberNo, pageable)
                .map(NotificationResponse::new);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long memberNo) {
        return notificationRepository
                .countByReceiver_MemberNoAndReadAtIsNull(memberNo);
    }

    @Transactional
    public void markRead(Long memberNo, Long notificationNo) {
        NotificationEntity notification = notificationRepository.getOrThrow(notificationNo);

        if (!notification.getReceiver().getMemberNo().equals(memberNo)) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markRead();
    }

    @Transactional
    public int markAllRead(Long memberNo) {
        return notificationRepository
                .markAllRead(memberNo, LocalDateTime.now());
    }
}

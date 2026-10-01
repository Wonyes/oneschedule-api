package com.studio.core.domain.notification.entity;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.repository.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;

@DynamicUpdate   // 바뀐 컬럼만 UPDATE
@Entity
@Table(name = "tb_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_no", nullable = false)
    private MemberEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_no")
    private MemberEntity sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 200)
    private String content;

    @Column(name = "target_no")
    private Long targetNo;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** 일정 알림만 채운다 — 클릭했을 때 그 날짜의 일간 뷰로 보내기 위해 */
    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @Builder
    public NotificationEntity(
            MemberEntity receiver, MemberEntity sender, NotificationType type,
            String title, String content, Long targetNo, LocalDate scheduleDate
    ) {
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetNo = targetNo;
        this.scheduleDate = scheduleDate;
    }

    public static NotificationEntity create(
        MemberEntity receiver, MemberEntity sender, NotificationType type,
        String title, String content, Long targetNo, LocalDate scheduleDate
    ) {
        return NotificationEntity.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .title(title)
                .content(content != null ? content : "")
                .targetNo(targetNo)
                .scheduleDate(scheduleDate)
                .build();
    }

    public void markRead() { if (readAt == null) readAt = LocalDateTime.now(); }
}

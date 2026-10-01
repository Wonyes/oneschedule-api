package com.studio.core.domain.notification.response;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.notification.entity.NotificationEntity;
import com.studio.core.global.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private final Long notificationNo;
    private final NotificationType type;
    private final String title;
    private final String content;
    private final Long targetNo;
    private final boolean read;
    private final String senderNickname;
    private final String senderProfileImageUrl;
    private final LocalDateTime createdAt;
    private final LocalDate scheduleDate;

    public NotificationResponse(NotificationEntity notification) {
        this.notificationNo = notification.getNotificationNo();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.targetNo = notification.getTargetNo();
        this.read = notification.getReadAt() != null;
        this.createdAt = notification.getCreateAt();
        this.scheduleDate = notification.getScheduleDate();

        MemberEntity sender = notification.getSender();
        this.senderNickname = sender != null ? sender.getNickname() : null;
        this.senderProfileImageUrl = sender != null ? sender.getProfileImageUrl() : null;
    }
}

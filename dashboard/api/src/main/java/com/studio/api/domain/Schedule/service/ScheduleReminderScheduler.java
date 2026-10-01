package com.studio.api.domain.Schedule.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.core.domain.Schedule.entity.ScheduleEntity;
import com.studio.core.domain.Schedule.repository.ScheduleRepository;
import com.studio.core.global.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScheduleReminderScheduler {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void remind() {
        LocalDateTime target = LocalDateTime.now().plusHours(1);
        List<ScheduleEntity> due = scheduleRepository.findDueForReminder(
                target.toLocalDate(), target.toLocalTime().withSecond(0), target.toLocalTime().withSecond(59));

        if (due.isEmpty()) return;

        // 일정이 몇 개든 참여자 조회는 2번으로 고정
        Map<Long, List<Long>> receivers = scheduleService.participantNosBySchedule(due);

        for (ScheduleEntity s : due) {
            notificationService.send(
                    receivers.getOrDefault(s.getId(), List.of()), null,
                    NotificationType.GROUP_SCHEDULE_REMINDER,
                    NotificationType.GROUP_SCHEDULE_REMINDER.message(s.getTitle(), ScheduleService.when(s)),
                    s.getGroup().getGroupNo(), s.getStartDate());
            s.markReminded();
        }
    }
}

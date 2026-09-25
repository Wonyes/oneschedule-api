package com.studio.core.domain.Schedule.entity;

import com.studio.core.domain.Schedule.dto.request.ScheduleRequest;
import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.member.entity.MemberEntity;
import jakarta.persistence.*;
import com.studio.core.global.enums.ScheduleCategory;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import org.hibernate.annotations.DynamicUpdate;

@DynamicUpdate   // 바뀐 컬럼만 UPDATE
@Entity
@Table(name = "tb_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // 탈퇴 시 그룹 일정은 남기고 작성자만 끊으므로 null을 허용한다
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_no")
    private GroupEntity group;

    private String title;

    private String content;

    private ScheduleCategory category;


    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime remindedAt;

    public void markReminded() {
        this.remindedAt = LocalDateTime.now();
    }

    @Builder
    public ScheduleEntity(
            MemberEntity member,
            GroupEntity group,
            String title,
            String content,
            ScheduleCategory category,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.member = member;
        this.group = group;
        this.title = title;
        this.content = content;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ScheduleEntity createPersonal(
            ScheduleRequest request,
            MemberEntity member
    ) {
        return ScheduleEntity.builder()
                .member(member)
                .title(request.title())
                .content(request.content())
                .category(ScheduleCategory.from(request.category()))
                .startDate(request.startDate())
                .endDate(resolveEndDate(request.startDate(), request.endDate()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }

    public static ScheduleEntity createGroupSchedule(
            ScheduleRequest request,
            MemberEntity member,
            GroupEntity group
    ) {
        return ScheduleEntity.builder()
                .member(member)
                .group(group)
                .title(request.title())
                .content(request.content())
                .category(ScheduleCategory.from(request.category()))
                .startDate(request.startDate())
                .endDate(resolveEndDate(request.startDate(), request.endDate()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }

    public void update(ScheduleRequest request) {
        boolean whenChanged = !Objects.equals(startDate, request.startDate())
                || !Objects.equals(startTime, request.startTime());

        this.title = request.title();
        this.content = request.content();
        this.category = ScheduleCategory.from(request.category());
        this.startDate = request.startDate();
        this.endDate = resolveEndDate(request.startDate(), request.endDate());
        this.startTime = request.startTime();
        this.endTime = request.endTime();

        if (whenChanged) this.remindedAt = null;
    }

    private static LocalDate resolveEndDate(LocalDate startDate, LocalDate endDate) {
        return endDate != null ? endDate : startDate;
    }


}
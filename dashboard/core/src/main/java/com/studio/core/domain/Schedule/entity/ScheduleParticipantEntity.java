package com.studio.core.domain.Schedule.entity;

import com.studio.core.domain.group.entity.GroupMemberEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_schedule_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleParticipantNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_no", nullable = false)
    private GroupMemberEntity groupMember;

    @Builder
    public ScheduleParticipantEntity(
            ScheduleEntity schedule,
            GroupMemberEntity groupMember
    ) {
        this.schedule = schedule;
        this.groupMember = groupMember;
    }
}

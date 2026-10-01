package com.studio.core.domain.Schedule.repository;

import com.studio.core.domain.Schedule.entity.ScheduleEntity;
import com.studio.core.domain.Schedule.entity.ScheduleParticipantEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipantEntity, Long> {

    @Query("""
    select p from ScheduleParticipantEntity p
    join fetch p.groupMember gm
    join fetch gm.member
    where p.schedule in :schedules
""")
    List<ScheduleParticipantEntity> findBySchedules(@Param("schedules") Collection<ScheduleEntity> schedules);

    void deleteAllBySchedule(ScheduleEntity schedule);

    /** 그룹원이 나가거나 내보내질 때 그 사람의 참여 기록을 먼저 지운다 (FK). */
    void deleteAllByGroupMember(GroupMemberEntity groupMember);

    /** 그룹 해체 시 그 그룹 일정의 참여 기록을 전부 지운다 (FK). */
    @Modifying
    @Query("delete from ScheduleParticipantEntity p where p.groupMember.group.groupNo = :groupNo")
    void deleteAllByGroupNo(@Param("groupNo") Long groupNo);
}

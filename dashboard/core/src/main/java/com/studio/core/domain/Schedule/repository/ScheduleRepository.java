package com.studio.core.domain.Schedule.repository;

import com.studio.core.domain.Schedule.entity.ScheduleEntity;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    /** 개인 일정 목록. 응답 조립에서 member를 읽으므로 같이 가져온다. */
    @Query("""
        select s
        from ScheduleEntity s
        join fetch s.member
        where s.member.memberNo = :memberNo
          and s.group is null
    """)
    List<ScheduleEntity> findPersonalWithMember(@Param("memberNo") Long memberNo);

    /** 그룹 일정 목록. member·group을 함께 가져와 지연 로딩을 막는다. */
    @Query("""
        select s
        from ScheduleEntity s
        left join fetch s.member
        join fetch s.group
        where s.group.groupNo = :groupNo
    """)
    List<ScheduleEntity> findByGroupNoWithMember(@Param("groupNo") Long groupNo);

    /** 그룹 해체 시 그룹 일정을 전부 지운다 (참여자 삭제 후 호출). */
    @Modifying
    @Query("delete from ScheduleEntity s where s.group.groupNo = :groupNo")
    void deleteAllByGroupNo(@Param("groupNo") Long groupNo);

    /** 리마인더 대상: 아직 안 보낸 그룹 일정 중 [from, to] 사이에 시작하는 것. */
    @Query("""
        select s
        from ScheduleEntity s
        join fetch s.group
        where s.group is not null
          and s.remindedAt is null
          and s.startDate = :date
          and s.startTime between :from and :to
    """)
    List<ScheduleEntity> findDueForReminder(
            @Param("date") LocalDate date,
            @Param("from") LocalTime from,
            @Param("to") LocalTime to
    );

    default ScheduleEntity getOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }
    /** 탈퇴: 그룹 일정은 남기고 작성자 표시만 지운다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ScheduleEntity s set s.member = null where s.member.memberNo = :memberNo and s.group is not null")
    void detachAuthor(@Param("memberNo") Long memberNo);

    /** 탈퇴: 개인 일정은 통째로 지운다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ScheduleEntity s where s.member.memberNo = :memberNo and s.group is null")
    void deletePersonalOf(@Param("memberNo") Long memberNo);

}

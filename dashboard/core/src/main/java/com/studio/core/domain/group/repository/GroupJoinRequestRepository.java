package com.studio.core.domain.group.repository;

import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupJoinRequestEntity;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.JoinRequestStatus;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;

public interface GroupJoinRequestRepository
        extends JpaRepository<GroupJoinRequestEntity, Long> {

    Optional<GroupJoinRequestEntity> findByGroupAndMember(
            GroupEntity group,
            MemberEntity member
    );

    @Query(value = """
    select jr
    from GroupJoinRequestEntity jr
    join fetch jr.member
    where jr.group.groupNo = :groupNo
      and jr.status = :status
    order by jr.createdAt, jr.requestNo
""",
            countQuery = """
    select count(jr)
    from GroupJoinRequestEntity jr
    where jr.group.groupNo = :groupNo
      and jr.status = :status
""")
    Page<GroupJoinRequestEntity> findByGroupAndStatus(
            @Param("groupNo") Long groupNo,
            @Param("status") JoinRequestStatus status,
            Pageable pageable
    );

    @Query("""
        select jr.group.groupNo
        from GroupJoinRequestEntity jr
        where jr.member.memberNo = :memberNo
          and jr.status = :status
    """)
    Set<Long> findGroupNosByMemberAndStatus(
            @Param("memberNo") Long memberNo,
            @Param("status") JoinRequestStatus status
    );

    void deleteAllByGroup(GroupEntity group);

    default GroupJoinRequestEntity getOrThrow(Long requestNo) {
        return findById(requestNo)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
    }
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GroupJoinRequestEntity r where r.member.memberNo = :memberNo")
    void deleteAllByMember(@Param("memberNo") Long memberNo);

}

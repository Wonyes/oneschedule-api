package com.studio.core.domain.group.repository;

import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupMemberRepository
        extends JpaRepository<GroupMemberEntity, Long> {

    @Query(value = """
        select gm
        from GroupMemberEntity gm
        join fetch gm.group g
        where gm.member.memberNo = :memberNo
        order by g.groupNo
    """,
            countQuery = """
        select count(gm)
        from GroupMemberEntity gm
        where gm.member.memberNo = :memberNo
    """)
    Page<GroupMemberEntity> findMyGroups(
            @Param("memberNo") Long memberNo,
            Pageable pageable
    );

    @Query("""
        select gm
        from GroupMemberEntity gm
        join fetch gm.member
        where gm.group.groupNo in :groupNos
        order by
            gm.group.groupNo,
            case
                when gm.groupRole = 'SUPER' then 0
                when gm.groupRole = 'SUB' then 1
                else 2
            end,
            gm.groupMemberNo
    """)
    List<GroupMemberEntity> findMembersOfGroups(
            @Param("groupNos") Collection<Long> groupNos
    );

    @Query("select gm.group.groupNo from GroupMemberEntity gm where gm.member.memberNo = :memberNo")
    Set<Long> findGroupNosByMemberNo(@Param("memberNo") Long memberNo);

    /** 참여자 저장용: 그룹 안에서 여러 멤버를 한 번에 찾는다. */
    @Query("""
        select gm
        from GroupMemberEntity gm
        join fetch gm.member
        where gm.group.groupNo = :groupNo
          and gm.member.memberNo in :memberNos
    """)
    List<GroupMemberEntity> findByGroupNoAndMemberNos(
            @Param("groupNo") Long groupNo,
            @Param("memberNos") Collection<Long> memberNos
    );

    Optional<GroupMemberEntity>
    findByGroup_GroupNoAndMember_MemberNo(
            Long groupNo,
            Long memberNo
    );

    boolean existsByGroupAndMember(
            GroupEntity group,
            MemberEntity member
    );

    void deleteAllByGroup(GroupEntity group);


    default GroupMemberEntity getOrThrow(Long groupNo, Long memberNo) {
        return findByGroup_GroupNoAndMember_MemberNo(groupNo, memberNo)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
    }
    /** 탈퇴 처리용 — 내가 속한 모든 그룹의 멤버 행 */
    List<GroupMemberEntity> findAllByMember_MemberNo(Long memberNo);

    /** 승계 대상 찾기 — 그룹장 제외, 관리자 우선, 오래된 순 */
    @Query("""
        select gm from GroupMemberEntity gm
        join fetch gm.member
        where gm.group.groupNo = :groupNo and gm.member.memberNo <> :memberNo
        order by case when gm.groupRole = com.studio.core.global.enums.GroupRole.SUB then 0 else 1 end,
                 gm.groupMemberNo
    """)
    List<GroupMemberEntity> findSuccessorCandidates(@Param("groupNo") Long groupNo, @Param("memberNo") Long memberNo);

}

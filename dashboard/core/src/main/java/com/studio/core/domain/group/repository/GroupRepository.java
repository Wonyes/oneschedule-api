package com.studio.core.domain.group.repository;

import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.response.PublicGroupResponse;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    boolean existsByGroupNameAndGroupNoNot(
            String groupName,
            Long groupNo
    );

    @Query("""
    select case when count(g) > 0 then true else false end
    from GroupEntity g
    where lower(replace(g.groupName, ' ', '')) = :groupName
""")
    boolean existsByGroupName(
            @Param("groupName") String groupName
    );

    @Query(value = """
        select new com.studio.core.domain.group.response.PublicGroupResponse(
            g.groupNo,
            g.groupName,
            g.description,
            g.visibility,
            (select count(gm) from GroupMemberEntity gm where gm.group = g),
            g.profileImageUrl
        )
        from GroupEntity g
        where g.visibility <> com.studio.core.global.enums.GroupVisibility.PRIVATE
          and (:keyword is null or lower(g.groupName) like lower(concat('%', :keyword, '%')))
        order by (select count(gm2) from GroupMemberEntity gm2 where gm2.group = g) desc
    """,
            countQuery = """
        select count(g)
        from GroupEntity g
        where g.visibility <> com.studio.core.global.enums.GroupVisibility.PRIVATE
          and (:keyword is null or lower(g.groupName) like lower(concat('%', :keyword, '%')))
    """)
    Page<PublicGroupResponse> findPublicGroups(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<GroupEntity> findByGroupCode(String groupCode);

    default GroupEntity getOrThrow(Long groupNo) {
        return findById(groupNo)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
    }

    default GroupEntity getByCodeOrThrow(String groupCode) {
        return findByGroupCode(groupCode)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
    }
}

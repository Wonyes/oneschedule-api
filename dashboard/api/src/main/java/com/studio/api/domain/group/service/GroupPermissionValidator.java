package com.studio.api.domain.group.service;

import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.group.repository.GroupMemberRepository;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 요청자가 그룹에서 무엇을 할 수 있는지 검사한다. 단순 조회는 GroupMemberRepository.getOrThrow */
@Component
@RequiredArgsConstructor
public class GroupPermissionValidator {

    private final GroupMemberRepository groupMemberRepository;

    /** 그룹 멤버여야 한다 */
    public GroupMemberEntity validateMember(Long groupNo, Long memberNo) {
        return groupMemberRepository.getOrThrow(groupNo, memberNo);
    }

    /** 그룹장이어야 한다 */
    public GroupMemberEntity validateOwner(Long groupNo, Long memberNo) {
        GroupMemberEntity groupMember = validateMember(groupNo, memberNo);

        if (groupMember.getGroupRole() != GroupRole.SUPER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_ACCESS);
        }

        return groupMember;
    }

    /** 그룹장 또는 부관리자여야 한다 */
    public void validateManager(Long groupNo, Long memberNo) {
        if (!validateMember(groupNo, memberNo).getGroupRole().isManager()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_ACCESS);
        }
    }
}

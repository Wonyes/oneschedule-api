package com.studio.core.domain.group.response;

import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.enums.GroupVisibility;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupResponse {

    private Long groupNo;

    private String groupName;

    private String groupCode;

    private GroupRole groupRole;

    private String position;

    private GroupVisibility visibility;

    private String description;

    private List<GroupMemberResponse> members;

    private String profileImageUrl;


    public static GroupResponse from(
            GroupEntity group,
            GroupMemberEntity groupMember
    ) {

        return from(group, groupMember, List.of());
    }

    public static GroupResponse from(
            GroupEntity group,
            GroupMemberEntity groupMember,
            List<GroupMemberResponse> members
    ) {

        return GroupResponse.builder()
                .groupNo(group.getGroupNo())
                .groupName(group.getGroupName())
                .groupCode(group.getGroupCode())
                .groupRole(groupMember.getGroupRole())
                .position(groupMember.getPosition())
                .visibility(group.getVisibility())
                .description(group.getDescription())
                .profileImageUrl(group.getProfileImageUrl())
                .members(members)
                .build();
    }
}
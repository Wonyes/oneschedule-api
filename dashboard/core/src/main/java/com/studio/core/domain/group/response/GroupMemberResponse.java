package com.studio.core.domain.group.response;

import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.global.enums.GroupRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberResponse {
    private Long memberNo;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private GroupRole groupRole;
    private String position;

    public static GroupMemberResponse from(
            GroupMemberEntity groupMember
    ) {
      return GroupMemberResponse.builder()
              .memberNo(groupMember.getMember().getMemberNo())
              .nickname(groupMember.getMember().getNickname())
              .email(groupMember.getMember().getEmail())
              .profileImageUrl(groupMember.getMember().getProfileImageUrl())
              .groupRole(groupMember.getGroupRole())
              .position(groupMember.getPosition())
              .build();
    }
}

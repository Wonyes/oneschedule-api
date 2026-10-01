package com.studio.core.domain.Schedule.dto.response;

import com.studio.core.domain.group.entity.GroupMemberEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleAuthorResponse {

    private Long memberNo;
    private String nickname;

    public static ScheduleAuthorResponse from(GroupMemberEntity groupMember) {
        return ScheduleAuthorResponse.builder()
                .memberNo(groupMember.getMember().getMemberNo())
                .nickname(groupMember.getMember().getNickname())
                .build();
    }
}

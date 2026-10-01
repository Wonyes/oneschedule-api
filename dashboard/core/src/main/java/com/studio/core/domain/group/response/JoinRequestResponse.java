package com.studio.core.domain.group.response;

import com.studio.core.domain.group.entity.GroupJoinRequestEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class JoinRequestResponse {

    private Long requestNo;
    private Long memberNo;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private String message;
    private LocalDateTime createdAt;

    public static JoinRequestResponse from(GroupJoinRequestEntity request) {
        return JoinRequestResponse.builder()
                .requestNo(request.getRequestNo())
                .memberNo(request.getMember().getMemberNo())
                .nickname(request.getMember().getNickname())
                .email(request.getMember().getEmail())
                .profileImageUrl(request.getMember().getProfileImageUrl())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .build();
    }
}

package com.studio.core.domain.member.dto.member.response;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.AuthProvider;
import lombok.Getter;

@Getter
public class MemberResponse {

    private final Long memberNo;
    private final String email;
    private final String nickname;
    private String phoneNumber;
    private String name;
    private String profileImageUrl;
    private AuthProvider provider;

    public MemberResponse(
            MemberEntity member
    ) {
        this.memberNo = member.getMemberNo();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.phoneNumber = member.getPhoneNumber();
        this.name = member.getName();
        this.profileImageUrl = member.getProfileImageUrl();
        this.provider = member.getProvider();
    }
}
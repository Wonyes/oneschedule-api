package com.studio.core.domain.member.dto.member.request;

import jakarta.validation.constraints.Size;

public record MemberPatchRequest(
        @Size(
                min = 2,
                max = 10,
                message = "닉네임은 2~10자여야 합니다."
        )
        String nickname,

        String phoneNumber,

        @Size(min= 2, max= 20, message = "이름은 2자에서 20자까지 가능합니다.")
        String name
) {
}

package com.studio.core.global.enums;

import lombok.Getter;

@Getter
public enum GroupVisibility {
    PUBLIC_OPEN("공개 · 즉시 가입"),
    PUBLIC_APPROVAL("공개 · 승인 후 가입"),
    PRIVATE("비공개 · 초대 코드");

    private final String description;

    GroupVisibility(String description) {
        this.description = description;
    }

    public boolean isPublic() { return this != PRIVATE; }
}

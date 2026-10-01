package com.studio.core.global.enums;

public enum GroupRole {
    SUPER,
    SUB,
    MEMBER;

    /** 그룹장(SUPER) 또는 부관리자(SUB) */
    public boolean isManager() {
        return this == SUPER || this == SUB;
    }
    public boolean isOwner() { return this == SUPER; }
}

package com.studio.core.global.enums;

public enum AuthRole {

    ROLE_ROOT("상위관리자"),
    ROLE_MANAGE("하위관리자"),
    ROLE_USER("일반회원");

    public final String meaning;

    AuthRole(String meaning) { this.meaning = meaning; }
    public boolean isAdmin() { return this == ROLE_ROOT || this == ROLE_MANAGE; }
}

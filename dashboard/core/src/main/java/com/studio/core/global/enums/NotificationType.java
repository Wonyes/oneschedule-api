package com.studio.core.global.enums;

import lombok.Getter;

/** 알림 종류. title은 목록 라벨, template은 본문 — %s는 message() 인자 순서대로 */
@Getter
public enum NotificationType {
    // ── 개인 정보 ──
    PASSWORD_CHANGE("비밀번호 변경", "비밀번호가 바뀌었어요. 본인이 아니면 바로 다시 바꿔 주세요"),
    WELCOME("환영", "%s님, 환영해요! 첫 일정을 만들어 보세요"),

    // ── 그룹 가입 (닉네임, 그룹) ──
    GROUP_JOIN_REQUESTED("가입 신청", "%s님이 %s에 가입을 신청했어요"),   // 관리자에게
    GROUP_JOIN_APPROVED("가입 승인", "%s에 참여하게 됐어요"),              // 신청자에게 (그룹)
    GROUP_JOIN_REJECTED("가입 거절", "%s 가입 신청이 거절됐어요"),          // 신청자에게 (그룹)

    // ── 그룹 멤버 ──
    GROUP_MEMBER_JOINED("새 멤버", "%s님이 %s에 들어왔어요"),              // 관리자에게 (닉네임, 그룹)
    GROUP_MEMBER_LEFT("멤버 탈퇴", "%s님이 %s에서 나갔어요"),              // 관리자에게 (닉네임, 그룹)
    GROUP_MEMBER_REMOVED("그룹 탈퇴", "관리자가 %s에서 내보냈어요"),               // 당사자에게 (그룹), targetNo = null
    GROUP_ROLE_CHANGED("권한 변경", "%s에서 %s가 됐어요"),
    GROUP_OWNER_TRANSFERRED("그룹장 변경", "%s의 그룹장이 됐어요"),

    // ── 그룹 ──
    GROUP_DISBANDED("그룹 해체", "%s 그룹이 해체됐어요"),                   // 멤버 전원에게 (그룹), targetNo = null

    // ── 그룹 일정 (제목, 시각) ──
    GROUP_SCHEDULE_CREATED("새 그룹 일정", "%s · %s"),
    GROUP_SCHEDULE_UPDATED("일정 변경", "%s 일정이 %s으로 바뀌었어요"),
    GROUP_SCHEDULE_DELETED("일정 취소", "%s (%s) 일정이 취소됐어요"),
    GROUP_SCHEDULE_REMINDER("일정 알림", "1시간 뒤 %s · %s"),
    SCHEDULE_PARTICIPANT_ADDED("일정 참여", "%s 일정에 참여자로 추가됐어요 · %s"),
    SCHEDULE_PARTICIPANT_REMOVED("일정 제외", "%s 일정 참여자에서 제외됐어요");   // (제목)

    private final String title;
    private final String template;

    NotificationType(String title, String template) {
        this.title = title;
        this.template = template;
    }

    /** 템플릿의 %s를 순서대로 채운 본문 */
    public String message(Object... args) {
        return String.format(template, args);
    }
}

package com.studio.core.global.enums;

import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

/** 일정 카테고리. DB와 API에는 소문자 value로 오간다(프론트 EventCategory와 동일). */
@Getter
public enum ScheduleCategory {
    WORK("work", "업무"),
    PERSONAL("personal", "개인"),
    MEETING("meeting", "회의"),
    IMPORTANT("important", "중요");

    private final String value;
    private final String label;

    ScheduleCategory(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static ScheduleCategory from(String value) {
        return Arrays.stream(values())
                .filter(c -> c.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PARAMETER));
    }
}

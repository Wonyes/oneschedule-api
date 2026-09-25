package com.studio.core.global.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** 기존 컬럼 값(work/personal/…)을 그대로 쓰기 위해 enum ↔ 소문자 문자열로 변환한다. */
@Converter(autoApply = true)
public class ScheduleCategoryConverter implements AttributeConverter<ScheduleCategory, String> {

    @Override
    public String convertToDatabaseColumn(ScheduleCategory category) {
        return category == null ? null : category.getValue();
    }

    @Override
    public ScheduleCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return ScheduleCategory.WORK;
        try {
            return ScheduleCategory.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ScheduleCategory.WORK; // 모르는 옛 값은 업무로 취급
        }
    }
}

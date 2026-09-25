package com.studio.core.domain.Schedule.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ScheduleRequest (
     @NotBlank
     @Size(max = 30, message = "최대 글자수는 30글자입니다.")
     String title,
     @NotBlank
     String category,

     @Size(max = 200, message = "최대 글자수는 200글자입니다.")
     String content,

     LocalDate endDate,

     @NotNull
     LocalDate startDate,
     LocalTime startTime,
     LocalTime endTime,

     List<Long> participantMemberNos
) {

}


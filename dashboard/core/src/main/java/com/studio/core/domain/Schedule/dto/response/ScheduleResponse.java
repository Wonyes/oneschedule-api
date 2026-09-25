package com.studio.core.domain.Schedule.dto.response;

import com.studio.core.domain.Schedule.dto.ScheduleViewType;
import com.studio.core.domain.Schedule.entity.ScheduleEntity;
import com.studio.core.domain.group.response.GroupMemberResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ScheduleResponse {

    private Long id;

    private String title;
    private String content;
    private String category;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;

    private ScheduleAuthorResponse author;

    private List<GroupMemberResponse> participants;
    private ScheduleViewType type;

    public static ScheduleResponse from(
            ScheduleEntity entity,
            ScheduleAuthorResponse author,
            List<GroupMemberResponse> participants
    ) {
        return ScheduleResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getCategory().getValue())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .participants(participants)
                .createdAt(entity.getCreatedAt())
                .author(author)
                .type(entity.getGroup() != null ? ScheduleViewType.GROUP : ScheduleViewType.PERSONAL)
                .build();
    }
}
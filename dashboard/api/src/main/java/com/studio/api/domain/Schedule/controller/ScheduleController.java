package com.studio.api.domain.Schedule.controller;

import com.studio.api.global.auth.LoginMember;
import com.studio.core.domain.Schedule.dto.ScheduleViewType;
import com.studio.core.domain.Schedule.dto.request.ScheduleRequest;
import com.studio.api.domain.Schedule.service.ScheduleService;
import com.studio.core.domain.Schedule.dto.response.ScheduleResponse;
import com.studio.core.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name ="Schedule API", description = "할 일 관리 대시보드")
@RestController
@RequestMapping("/v1/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "일정 조회", description = "개인/그룹 일정 조회 (type: PERSONAL(기본값), GROUP)")
    @GetMapping
    public SuccessResponse<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = false) Long groupNo,
            @LoginMember Long memberNo,
            @RequestParam(required = false, defaultValue = "PERSONAL") ScheduleViewType type
    ) {
        return SuccessResponse.ok(
                scheduleService.getSchedules(groupNo, memberNo, type)
        );
    }

    @Operation(summary = "개인 일정 등록")
    @PostMapping
    public SuccessResponse<ScheduleResponse> createSchedule(
            @LoginMember Long memberNo,
            @Validated @RequestBody ScheduleRequest request
    ) {

        return SuccessResponse.ok(
                scheduleService.createSchedule(
                        memberNo,
                        request
                )
        );
    }

    @Operation(summary = "그룹 일정 등록")
    @PostMapping("/group")
    public SuccessResponse<ScheduleResponse> createGroupSchedule(
            @LoginMember Long memberNo,
            @RequestParam Long groupNo,
            @Validated @RequestBody ScheduleRequest request
    ) {

        return SuccessResponse.ok(
                scheduleService.createGroupSchedule(
                        memberNo,
                        groupNo,
                        request
                )
        );
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{id}")
    public SuccessResponse<?> updateSchedule(
            @PathVariable Long id,
            @LoginMember Long memberNo,
            @Validated @RequestBody ScheduleRequest req
    ) {
        return SuccessResponse.ok(scheduleService.updateSchedule(id, memberNo, req));
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    public SuccessResponse<?> deleteSchedule(
            @PathVariable Long id,
            @LoginMember Long memberNo
    ) {
        scheduleService.deleteSchedule(id, memberNo);
        return SuccessResponse.ok();
    }
}

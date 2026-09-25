package com.studio.api.domain.notification.controller;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.api.global.auth.LoginMember;
import com.studio.core.domain.notification.response.NotificationResponse;
import com.studio.core.global.response.PageResponse;
import com.studio.core.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록")
    @GetMapping
    public SuccessResponse<PageResponse<NotificationResponse>> getMine(
            @LoginMember Long memberNo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return SuccessResponse.ok(
                PageResponse.from(
                        notificationService.getMine(
                                memberNo, pageable
                        )
                )
        );
    }

    @Operation(summary = "안 읽은 알림 개수")
    @GetMapping("/unread-count")
    public SuccessResponse<Long> countUnread(@LoginMember Long memberNo) {
        return SuccessResponse.ok(
                notificationService.countUnread(memberNo)
        );
    }

    @Operation(summary = "알림 읽음")
    @PatchMapping("/{notificationNo}/read")
    public SuccessResponse<Void> markRead(
            @LoginMember Long memberNo,
            @PathVariable Long notificationNo
    ) {
        notificationService.markRead(memberNo, notificationNo);
        return SuccessResponse.ok();
    }

    @Operation(summary = "알림 모두 읽음")
    @PatchMapping("/read-all")
    public SuccessResponse<Integer> markAllRead(@LoginMember Long memberNo) {
        return SuccessResponse.ok(
                notificationService.markAllRead(memberNo)
        );
    }

}


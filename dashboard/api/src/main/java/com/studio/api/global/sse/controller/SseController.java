package com.studio.api.global.sse.controller;


import com.studio.api.global.auth.LoginMember;
import com.studio.api.global.sse.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterRegistry registry;

    @Operation(summary = "실시간 연결 구독")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @LoginMember Long memberNo
    ) {
        return registry.add(memberNo);
    }
}

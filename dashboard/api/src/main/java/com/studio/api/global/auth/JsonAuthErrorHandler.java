package com.studio.api.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.core.global.exception.ErrorCode;
import com.studio.core.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security가 직접 내는 401/403도 ErrorResponse 틀로 내려준다.
 * 필터가 토큰 검증 실패 원인(만료/위조)을 request attribute로 남기면 그 코드를 우선 쓴다.
 */
@Component
@RequiredArgsConstructor
public class JsonAuthErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    public static final String AUTH_ERROR_ATTRIBUTE = "authErrorCode";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e
    ) throws IOException {
        Object detail = request.getAttribute(AUTH_ERROR_ATTRIBUTE);
        ErrorCode code = detail instanceof ErrorCode ec ? ec : ErrorCode.UNAUTHORIZED;
        write(response, code);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException e
    ) throws IOException {
        write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code));
    }
}

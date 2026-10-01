package com.studio.core.global.response;

import com.studio.core.global.response.result.ResponseState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SuccessResponse<T> {

    @Schema(description = "성공 여부", nullable = false, example = "true")
    private boolean success = true;
    @Schema(description = "상태 코드", nullable = false, example = "1")
    private int code;
    @Schema(description = "응답 메세지", nullable = false, example = "성공하였습니다.")
    private String message;
    @Schema(description = "응답 데이터", nullable = false)
    private T result;

    public SuccessResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> SuccessResponse<T> of(int code, String message, T data){
        return new SuccessResponse<>(code, message, data);
    }

    public static <T> SuccessResponse<T> ok(T data) {
        return of(ResponseState.SUCCESS.getCode(), ResponseState.SUCCESS.getMessage(), data);
    }

    public static <T> SuccessResponse<T> ok() {
        return of(ResponseState.SUCCESS.getCode(), ResponseState.SUCCESS.getMessage(), null);
    }


    public static <T> SuccessResponse<T> customCode(int code,T data) {
        return of(code, ResponseState.SUCCESS.getMessage(), data);
    }


}

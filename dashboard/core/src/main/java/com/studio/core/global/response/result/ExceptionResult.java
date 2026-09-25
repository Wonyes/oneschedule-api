package com.studio.core.global.response.result;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ExceptionResult {
    @Builder
    @Getter
    @Setter
    public static class ParameterData {
        private String key;
        private String value;
        private String reason;
    }

    @Builder
    @Getter
    @Setter
    public static class ServerErrorData {
        private String errorClass;
        private String errorMessage;
        private String errorLocation;
    }
}

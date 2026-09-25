package com.studio.core.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private Exception originException;
    private String addMessage = null;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String addMessage){
        super(addMessage);
        this.errorCode = errorCode;
        this.addMessage = addMessage;
    }

    public CustomException(ErrorCode errorCode, Exception exception) {
        super(exception);
        this.errorCode = errorCode;
        this.originException = exception;
    }
}

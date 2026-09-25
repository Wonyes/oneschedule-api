package com.studio.core.global.exception;

import com.studio.core.global.response.ErrorResponse;
import com.studio.core.global.response.result.ExceptionResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException e
    ) {
        String cause = e.getMostSpecificCause().getMessage();

        log.error("[DATA INTEGRITY] message: [{}]", cause, e);

        boolean duplicated = cause != null && cause.contains("ORA-00001");

        ErrorCode errorCode = duplicated
                ? ErrorCode.DATA_CONFLICT
                : ErrorCode.DATA_INTEGRITY_ERROR;

        return ResponseEntity
                .status(errorCode.getHttpCode())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ErrorResponse<Void> handleNotFound(Exception e) {
        log.warn("[NOT FOUND PATH] message: [{}]", e.getMessage());

        return ErrorResponse.of(ErrorCode.NOT_FOUND_PATH);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ErrorResponse<Void> handleUntrackedException(Exception e) {
        log.error("[UNTRACKED ERROR] class: [{}], message: [{}]",
                e.getClass().getSimpleName(),
                e.getMessage(),
                e);

        return ErrorResponse.of(ErrorCode.SERVER_UNTRACKED_ERROR);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse<List<ExceptionResult.ParameterData>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("[PARAMETER VALIDATION EXCEPTION] class: [{}], message: [{}]", e.getClass().getSimpleName(), e.getMessage());

        List<ExceptionResult.ParameterData> list = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            list.add(ExceptionResult.ParameterData.builder()
                    .key(fieldError.getField())
                    .value(fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString())
                    .reason(fieldError.getDefaultMessage())
                    .build());
        }

        return ErrorResponse.of(ErrorCode.PARAMETER_VALIDATION_ERROR, list);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse<List<ExceptionResult.ParameterData>> handleConstraintViolationExceptions(ConstraintViolationException e) {
        log.warn("[PARAMETER VALIDATION EXCEPTION] class: [{}], message: [{}]", e.getClass().getSimpleName(), e.getMessage());

        List<ExceptionResult.ParameterData> list = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            list.add(ExceptionResult.ParameterData.builder()
                    .key(violation.getPropertyPath().toString())
                    .value(violation.getInvalidValue() == null ? null : violation.getInvalidValue().toString())
                    .reason(violation.getMessage())
                    .build());
        }
        return ErrorResponse.of(ErrorCode.PARAMETER_VALIDATION_ERROR, list);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse<List<ExceptionResult.ParameterData>> handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("[MISSING PARAMETER] name: [{}], type: [{}]", e.getParameterName(), e.getParameterType());

        List<ExceptionResult.ParameterData> list = List.of(
                ExceptionResult.ParameterData.builder()
                        .key(e.getParameterName())
                        .reason("필수 파라미터가 누락되었습니다.")
                        .build()
        );

        return ErrorResponse.of(ErrorCode.NO_REQUIRED_VALUE, list);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse<List<ExceptionResult.ParameterData>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("[TYPE MISMATCH] name: [{}], value: [{}]", e.getName(), e.getValue());

        List<ExceptionResult.ParameterData> list = List.of(
                ExceptionResult.ParameterData.builder()
                        .key(e.getName())
                        .value(e.getValue() == null ? null : e.getValue().toString())
                        .reason("허용되지 않는 값입니다.")
                        .build()
        );

        return ErrorResponse.of(ErrorCode.INVALID_TYPE_PARAMETER, list);
    }

    @ExceptionHandler({MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse<Void> handleMissingPartException(MissingServletRequestPartException e) {
        log.warn("[MISSING PART] name: [{}]", e.getRequestPartName());
        return ErrorResponse.of(ErrorCode.NO_REQUIRED_VALUE.getErrorCode(), "업로드할 파일이 없습니다.");
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("[UPLOAD TOO LARGE] max: [{}]", e.getMaxUploadSize());
        return ErrorResponse.of(ErrorCode.INVALID_PARAMETER.getErrorCode(), "업로드 가능한 파일 크기를 초과했습니다.");
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(METHOD_NOT_ALLOWED)
    public ErrorResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[METHOD NOT ALLOWED] method: [{}]", e.getMethod());
        return ErrorResponse.of(METHOD_NOT_ALLOWED.value(), "지원하지 않는 요청 방식입니다.");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse<Void> handleHttpMessageParsingExceptions(HttpMessageNotReadableException e) {
        log.warn("[PARAMETER GRAMMAR EXCEPTION] class: [{}], message: [{}]", e.getClass().getSimpleName(), e.getMessage());
        return ErrorResponse.of(ErrorCode.PARAMETER_GRAMMAR_ERROR);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse<Object>> handleCustomExceptions(CustomException e) {
        log.warn("[CUSTOM EXCEPTION] class: [{}], message: [{}]", e.getClass().getSimpleName(), e.getErrorCode().getMessage());

        ErrorResponse<Object> body = ErrorResponse.of(e.getErrorCode());

        if (e.getAddMessage() != null) {
            body = ErrorResponse.of(e.getErrorCode(), e.getAddMessage());
        }

        return new ResponseEntity<>(body, HttpStatus.valueOf(e.getErrorCode().getHttpCode()));
    }
}
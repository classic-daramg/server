package com.daramg.server.common.exception;

import com.daramg.server.common.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ErrorCodeRegistry errorCodeRegistry;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(BusinessException e) {
        BaseErrorCode errorCode = e.getErrorCode();

        log.warn("GeneralException: {} - {}", errorCode.getMessage(), e.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        // AuthenticationException의 원인 예외에서 BusinessException 확인
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof BusinessException businessException) {
                BaseErrorCode errorCode = businessException.getErrorCode();
                log.warn("BusinessException (from AuthenticationException): {}", errorCode.getMessage());
                return ResponseEntity
                        .status(errorCode.getHttpStatus())
                        .body(ErrorResponse.of(errorCode));
            }
            cause = cause.getCause();
        }
        
        // 기본 인증 실패 응답
        log.warn("AuthenticationException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(CommonErrorStatus.UNAUTHORIZED));
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // 예외 자체가 BusinessException인지 확인
        if (ex instanceof BusinessException businessException) {
            BaseErrorCode errorCode = businessException.getErrorCode();
            log.warn("BusinessException (from handleExceptionInternal, direct): {}", errorCode.getMessage());
            return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(ErrorResponse.of(errorCode));
        }
        
        // HandlerMethodArgumentResolver에서 발생한 BusinessException을 확인
        Throwable current = ex;
        int depth = 0;
        while (current != null && depth < 10) {
            if (current instanceof BusinessException businessException) {
                BaseErrorCode errorCode = businessException.getErrorCode();
                log.warn("BusinessException (from handleExceptionInternal, depth={}): {}", depth, errorCode.getMessage());
                return ResponseEntity
                        .status(errorCode.getHttpStatus())
                        .body(ErrorResponse.of(errorCode));
            }
            current = current.getCause();
            depth++;
        }
        
        log.warn("handleExceptionInternal: {} - {}", ex.getClass().getName(), ex.getMessage());
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("ConstraintViolationException: {}", e.getMessage());

        List<ErrorResponse.FieldErrorResponse> fieldErrors = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String field = propertyPath.contains(".")
                            ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                            : propertyPath;
                    return new ErrorResponse.FieldErrorResponse(
                            field,
                            violation.getInvalidValue() == null ? "" : violation.getInvalidValue().toString(),
                            violation.getMessage()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(CommonErrorStatus.BAD_REQUEST, fieldErrors));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());

        List<ErrorResponse.FieldErrorResponse> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String code = error.getDefaultMessage();
                    BaseErrorCode baseErrorCode = errorCodeRegistry.get(code);
                    String message = baseErrorCode != null 
                            ? baseErrorCode.getMessage() 
                            : error.getDefaultMessage();
                    return new ErrorResponse.FieldErrorResponse(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            message
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(CommonErrorStatus.BAD_REQUEST, fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        // 예외 자체가 BusinessException인지 확인
        if (e instanceof BusinessException businessException) {
            BaseErrorCode errorCode = businessException.getErrorCode();
            log.warn("BusinessException (direct): {}", errorCode.getMessage());
            return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(ErrorResponse.of(errorCode));
        }
        
        // HandlerMethodArgumentResolver에서 발생한 BusinessException을 확인
        Throwable current = e;
        int depth = 0;
        while (current != null && depth < 10) {
            if (current instanceof BusinessException businessException) {
                BaseErrorCode errorCode = businessException.getErrorCode();
                log.warn("BusinessException (in chain, depth={}): {}", depth, errorCode.getMessage());
                return ResponseEntity
                        .status(errorCode.getHttpStatus())
                        .body(ErrorResponse.of(errorCode));
            }
            current = current.getCause();
            depth++;
        }
        
        log.error("Unexpected Exception: {} - {}", e.getClass().getName(), e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(CommonErrorStatus.INTERNAL_SERVER_ERROR));
    }
}

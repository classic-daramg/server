package com.daramg.server.common.dto;

import com.daramg.server.common.exception.BaseErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private final String code;
    private final String message;
    private final List<FieldErrorResponse> fieldErrors;

    public static ErrorResponse of(BaseErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(BaseErrorCode errorCode, List<FieldErrorResponse> fieldErrors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .fieldErrors(fieldErrors)
                .build();
    }

    public record FieldErrorResponse(
            String field,
            String value,
            String reason
    ) {}
}

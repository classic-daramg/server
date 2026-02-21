package com.daramg.server.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommonErrorStatus implements BaseErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCategory.COMMON.generate(500), "서버 에러, 서버 관리자에게 문의 바랍니다."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, ErrorCategory.COMMON.generate(504), "타임아웃 에러, 서버 관리자에게 문의 바랍니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, ErrorCategory.COMMON.generate(400), "유효하지 않은 요청입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, ErrorCategory.COMMON.generate(400_1), "유효하지 않은 커서 포맷입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCategory.COMMON.generate(401), "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCategory.COMMON.generate(403), "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND , ErrorCategory.COMMON.generate(404), "찾을 수 없는 리소스입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

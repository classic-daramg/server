package com.daramg.server.auth.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthErrorStatus implements BaseErrorCode {

    CODE_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_1), "올바르지 않은 인증번호입니다."),
    EMAIL_NOT_EXIST(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_2), "이메일은 비어있을 수 없습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_3), "올바른 이메일 형식이 아닙니다."),
    VERIFICATION_CODE_NOT_EXIST(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_4), "인증번호는 비어있을 수 없습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_5), "인증번호는 6자리 숫자여야 합니다."),
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_6), "가입되지 않은 이메일입니다."),
    REJOIN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_7), "탈퇴 후 30일이 지나야 재가입할 수 있습니다."),

    TOKEN_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, ErrorCategory.AUTH.generate(401_1), "만료된 토큰입니다."),
    INVALID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, ErrorCategory.AUTH.generate(401_2), "유효하지 않은 토큰입니다."),
    INVALID_COOKIE_EXCEPTION(HttpStatus.UNAUTHORIZED, ErrorCategory.AUTH.generate(401_3), "유효하지 않은 쿠키입니다."),
    USER_NOT_ACTIVE(HttpStatus.UNAUTHORIZED, ErrorCategory.AUTH.generate(401_4), "비활성화된 사용자입니다."),

    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, ErrorCategory.AUTH.generate(404_1), "존재하지 않는 사용자입니다."),

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, ErrorCategory.AUTH.generate(409_1), "중복된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, ErrorCategory.AUTH.generate(409_2), "중복된 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_8), "비밀번호가 일치하지 않습니다."),
    UNSUPPORTED_EMAIL_PURPOSE(HttpStatus.BAD_REQUEST, ErrorCategory.AUTH.generate(400_9), "지원하지 않는 이메일 발송 목적입니다."),

    EMAIL_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, ErrorCategory.AUTH.generate(429_1), "잠시 후 다시 시도해주세요. (1분에 1회만 요청 가능합니다.)"),
    VERIFICATION_ATTEMPT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, ErrorCategory.AUTH.generate(429_2), "인증 시도 횟수를 초과했습니다. 새로운 인증번호를 요청해주세요."),

    SEND_VERIFICATION_EMAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCategory.AUTH.generate(500), "이메일 전송에 실패했습니다."),
    REDIS_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCategory.AUTH.generate(500_1), "Redis 연결에 실패했습니다. 서버 관리자에게 문의 바랍니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

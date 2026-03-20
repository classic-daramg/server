package com.daramg.server.banner.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BannerErrorStatus implements BaseErrorCode {

    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCategory.BANNER.generate(404_1), "배너를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

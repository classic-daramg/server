package com.daramg.server.post.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostErrorStatus implements BaseErrorCode {

    PRIMARY_COMPOSER_REQUIRED(HttpStatus.BAD_REQUEST, ErrorCategory.POST.generate(400_1), "발행 시 주요 작곡가는 필수입니다."),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, ErrorCategory.POST.generate(403_1), "포스트 작성자만 수정/삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

package com.daramg.server.notification.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationErrorStatus implements BaseErrorCode {

    NOT_OWN_NOTIFICATION(HttpStatus.FORBIDDEN, ErrorCategory.NOTIFICATION.generate(403_1), "본인의 알림만 처리할 수 있습니다."),
    NOT_NOTICE_AUTHOR(HttpStatus.FORBIDDEN, ErrorCategory.NOTIFICATION.generate(403_2), "공지사항의 작성자가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

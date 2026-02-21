package com.daramg.server.user.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserErrorStatus implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCategory.USER.generate(404_1), "유저를 찾을 수 없습니다."),
    SAME_EMAIL(HttpStatus.BAD_REQUEST, ErrorCategory.USER.generate(400_1), "기존 이메일과 동일한 이메일로 변경할 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, ErrorCategory.USER.generate(409_1), "이미 가입되어 있는 이메일입니다."),
    SELF_FOLLOW(HttpStatus.BAD_REQUEST, ErrorCategory.USER.generate(400_2), "팔로우 대상과 주체의 유저가 동일합니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, ErrorCategory.USER.generate(409_2), "이미 팔로우하고 있는 상태입니다."),
    SELF_UNFOLLOW(HttpStatus.BAD_REQUEST, ErrorCategory.USER.generate(400_3), "언팔로우 대상과 주체의 유저가 동일합니다."),
    NOT_FOLLOWING(HttpStatus.BAD_REQUEST, ErrorCategory.USER.generate(400_4), "팔로우하지 않은 유저입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

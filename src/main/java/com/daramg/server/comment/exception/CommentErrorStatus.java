package com.daramg.server.comment.exception;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.ErrorCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommentErrorStatus implements BaseErrorCode {

    BLOCKED_POST(HttpStatus.FORBIDDEN, ErrorCategory.COMMENT.generate(403_1), "블락된 포스트에는 댓글을 남길 수 없습니다."),
    BLOCKED_OR_DELETED_COMMENT_REPLY(HttpStatus.FORBIDDEN, ErrorCategory.COMMENT.generate(403_2), "삭제되었거나 블락된 댓글에는 대댓글을 남길 수 없습니다."),
    BLOCKED_OR_DELETED_COMMENT_LIKE(HttpStatus.FORBIDDEN, ErrorCategory.COMMENT.generate(403_3), "삭제되었거나 블락된 댓글에는 좋아요를 누를 수 없습니다."),
    ALREADY_DELETED(HttpStatus.BAD_REQUEST, ErrorCategory.COMMENT.generate(400_1), "이미 삭제 처리된 댓글입니다."),
    NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, ErrorCategory.COMMENT.generate(403_4), "댓글을 작성한 유저만 댓글을 삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

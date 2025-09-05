package com.daramg.server.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final static BaseErrorCode defaultErrorCode = CommonErrorStatus.INTERNAL_SERVER_ERROR;
    private final BaseErrorCode errorCode;

    public BusinessException() {
        this(defaultErrorCode);
    }

    public BusinessException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}

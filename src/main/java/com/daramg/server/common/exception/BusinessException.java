package com.daramg.server.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final static BaseErrorCode defaultErrorCode = CommonErrorStatus.INTERNAL_SERVER_ERROR;
    private final BaseErrorCode errorCode;

    public BusinessException() {
        super(defaultErrorCode.getMessage());
        this.errorCode = defaultErrorCode;
    }

    public BusinessException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = defaultErrorCode;
    }

    public BusinessException(String message, BaseErrorCode errorCode) {
        super(errorCode.getMessage() + message);
        this.errorCode = errorCode;
    }
}

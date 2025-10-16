package com.daramg.server.common.exception;

public class NotFoundException extends BusinessException {

    private final static BaseErrorCode defaultErrorCode = CommonErrorStatus.NOT_FOUND;
    private final BaseErrorCode errorCode;

    public NotFoundException() {
        super(defaultErrorCode.getMessage());
        this.errorCode = defaultErrorCode;
    }

    public NotFoundException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public NotFoundException(String message) {
        super(message);
        this.errorCode = defaultErrorCode;
    }
}

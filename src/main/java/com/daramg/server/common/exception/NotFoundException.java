package com.daramg.server.common.exception;

public class NotFoundException extends BusinessException {

    private final static BaseErrorCode defaultErrorCode = CommonErrorStatus.NOT_FOUND;

    public NotFoundException() {
        super(defaultErrorCode);
    }

    public NotFoundException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(String message) {
        super(message, defaultErrorCode);
    }
}

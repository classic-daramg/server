package com.daramg.server.common.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

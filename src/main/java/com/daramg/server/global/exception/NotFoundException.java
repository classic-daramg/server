package com.daramg.server.global.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

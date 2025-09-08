package com.daramg.server.common.exception;

public class UnauthorizedException extends BusinessException{
    public UnauthorizedException() {
        super(CommonErrorStatus.UNAUTHORIZED);
    }
}

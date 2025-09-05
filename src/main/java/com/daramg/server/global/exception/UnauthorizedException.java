package com.daramg.server.global.exception;

public class UnauthorizedException extends BusinessException{
    public UnauthorizedException() {
        super(CommonErrorStatus.UNAUTHORIZED);
    }
}

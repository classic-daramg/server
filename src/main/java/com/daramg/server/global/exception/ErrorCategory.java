package com.daramg.server.global.exception;

public enum ErrorCategory {

    COMMON("COMMON_"),
    USER("USER_"),
    POST("POST_");

    private final String prefix;

    ErrorCategory(String prefix){
        this.prefix = prefix;
    }

    public String generate(int codeNumber){
        return this.prefix + codeNumber;
    }
}

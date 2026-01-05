package com.daramg.server.common.exception;

public enum ErrorCategory {

    COMMON("COMMON_"),
    AUTH("AUTH_"),
    USER("USER_"),
    POST("POST_"),
    IMAGE("IMAGE_");

    private final String prefix;

    ErrorCategory(String prefix){
        this.prefix = prefix;
    }

    public String generate(int codeNumber){
        return this.prefix + codeNumber;
    }
}

package com.daramg.server.common.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ErrorCodeRegistry {

    private final Map<String, BaseErrorCode> registry = new HashMap<>();

    @Autowired
    public ErrorCodeRegistry(List<BaseErrorCode> errorCodes) {
        errorCodes.forEach(errorCode ->
                registry.put(errorCode.getCode(), errorCode)
        );
    }

    public BaseErrorCode get(String code) {
        return registry.get(code);
    }
}

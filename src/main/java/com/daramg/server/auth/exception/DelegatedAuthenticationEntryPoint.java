package com.daramg.server.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 보안구역인 Security Filter Chain 에서 발생한 예외를 넘겨,
 에러 응답이 @RestControllerAdvice에 정의된 일관된 형식을 가지도록 한다.
 */
@Component("delegatedAuthenticationEntryPoint")
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public DelegatedAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }
}

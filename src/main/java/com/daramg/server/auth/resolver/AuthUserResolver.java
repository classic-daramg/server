package com.daramg.server.auth.resolver;

import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException(AuthErrorStatus.INVALID_COOKIE_EXCEPTION);
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION);
    }
}

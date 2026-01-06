package com.daramg.server.auth.resolver;

import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        // @AuthenticationPrincipal 어노테이션이 있으면 Spring Security 기본 resolver 사용
        if (parameter.hasParameterAnnotation(AuthenticationPrincipal.class)) {
            return false;
        }
        return User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @Nullable
    public Object resolveArgument(@NotNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  @NotNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null; // 비로그인 사용자는 null 반환
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            if (!user.isActive()) {
                throw new BusinessException(AuthErrorStatus.USER_NOT_ACTIVE);
            }
            return user;
        }
        return null; // principal이 User가 아닌 경우 null 반환 (비로그인 사용자)
    }
}

package com.daramg.server.auth.filter;

import com.daramg.server.auth.application.AuthService;
import com.daramg.server.auth.util.CookieUtil;
import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.UnauthorizedException;
import com.daramg.server.domain.user.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtTokenProvider;
    private final AuthenticationEntryPoint authEntryPoint;
    private final AuthService authService;

    @Value("${cookie.access-name}")
    public String ACCESS_COOKIE_NAME;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws IOException, ServletException {
        try{
            Cookie cookie = CookieUtil.getCookie(request, ACCESS_COOKIE_NAME)
                    .orElse(null);

            if (cookie != null) {
                Authentication authentication = authenticate(cookie.getValue());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (BusinessException e){
            log.debug("JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(request, response, new AuthenticationException(e.getMessage(), e) {});
        } catch (AuthenticationException e){
            log.debug("인증 예외 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(request, response, e);
        } catch (Exception e) {
            log.error("JWT 필터에서 예외 발생: {}", e.getMessage());
            throw e;
        }
    }

    private Authentication authenticate(String accessToken) {
        jwtTokenProvider.validateAccessToken(accessToken);
        String email = jwtTokenProvider.getUserEmail(accessToken);

        User user = authService.loadUserByEmail(email);
        return new UsernamePasswordAuthenticationToken(user, null);

        //TODO: role
//        String role = jwtTokenProvider.getRole(accessToken);
//        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

}

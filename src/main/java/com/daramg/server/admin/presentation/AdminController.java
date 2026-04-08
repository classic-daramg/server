package com.daramg.server.admin.presentation;

import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.auth.util.CookieUtil;
import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.exception.UserErrorStatus;
import com.daramg.server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Value("${jwt.access-time}")
    private long accessTokenLifetimeInMillis;

    @Value("${cookie.access-name}")
    private String ACCESS_COOKIE_NAME;

    @Value("${cookie.domain:}")
    private String cookieDomain;

    private static final String ADMIN_BACKUP_COOKIE_NAME = "admin_backup";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/impersonate/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void impersonate(
            @PathVariable Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 현재 어드민 AT를 백업 쿠키에 저장
        String adminToken = CookieUtil.getCookie(request, ACCESS_COOKIE_NAME)
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));

        ResponseCookie backupCookie = CookieUtil.createCookie(
                ADMIN_BACKUP_COOKIE_NAME, adminToken, accessTokenLifetimeInMillis * 8, cookieDomain);
        response.addHeader(HttpHeaders.SET_COOKIE, backupCookie.toString());

        String impersonatedToken = jwtUtil.createAccessToken(targetUser);
        ResponseCookie accessCookie = CookieUtil.createCookie(
                ACCESS_COOKIE_NAME, impersonatedToken, accessTokenLifetimeInMillis, cookieDomain);
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    @PostMapping("/revert")
    @ResponseStatus(HttpStatus.OK)
    public void revert(HttpServletRequest request, HttpServletResponse response) {
        String adminToken = CookieUtil.getCookie(request, ADMIN_BACKUP_COOKIE_NAME)
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION));

        ResponseCookie accessCookie = CookieUtil.createCookie(
                ACCESS_COOKIE_NAME, adminToken, accessTokenLifetimeInMillis, cookieDomain);
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie deleteBackup = CookieUtil.deleteCookie(ADMIN_BACKUP_COOKIE_NAME, cookieDomain);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteBackup.toString());
    }
}

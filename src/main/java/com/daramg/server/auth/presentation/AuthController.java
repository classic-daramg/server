package com.daramg.server.auth.presentation;

import com.daramg.server.auth.application.AuthService;
import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.dto.*;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.auth.util.CookieUtil;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.dto.PasswordRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${jwt.access-time}")
    private long accessTokenLifetimeInMillis;

    @Value("${cookie.access-name}")
    private String ACCESS_COOKIE_NAME;

    @Value("${cookie.refresh-name}")
    private String REFRESH_COOKIE_NAME;

    @Value("${jwt.refresh-time}")
    private long refreshTokenLifetimeInMillis;

    private final MailVerificationService mailVerificationService;
    private final AuthService authService;

    @PostMapping("/email-verifications")
    @ResponseStatus(HttpStatus.OK)
    public void sendVerificationEmail(@RequestBody @Valid EmailVerificationRequestDto request) {
        mailVerificationService.sendVerificationEmail(request);
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public void verify(@RequestBody @Valid CodeVerificationRequestDto request) {
        mailVerificationService.verifyEmailWithCode(request);
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(
            @RequestPart("signupRequest") @Valid SignupRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        authService.signup(request, image);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        TokenResponseDto tokenResponse = authService.login(request);
        setAccessTokenCookie(response, tokenResponse.getAccessToken()); // AT 쿠키
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken()); // RT 쿠키
        return tokenResponse;
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletResponse response, User user) {
        authService.logout(user);
        clearAuthCookies(response);
    }

    @DeleteMapping("/signout")
    @ResponseStatus(HttpStatus.OK)
    public void signOut(@RequestBody @Valid PasswordRequestDto request, HttpServletResponse response, User user) {
        authService.signOut(user, request);
        clearAuthCookies(response);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, REFRESH_COOKIE_NAME)
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION));

        String refreshToken = refreshTokenCookie.getValue();
        TokenResponseDto tokenResponse = authService.refreshAccessToken(refreshToken);

        setAccessTokenCookie(response, tokenResponse.getAccessToken()); // AT 쿠키
    }

    /**
     비밀번호 초기화 시 자동 로그아웃
     */
    @PutMapping("/password-reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody com.daramg.server.auth.dto.PasswordRequestDto request, HttpServletResponse response){
        authService.resetPassword(request);
        clearAuthCookies(response);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie accessCookie = CookieUtil.createCookie(
                ACCESS_COOKIE_NAME,
                accessToken,
                accessTokenLifetimeInMillis
        );
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshCookie = CookieUtil.createCookie(
                REFRESH_COOKIE_NAME,
                refreshToken,
                refreshTokenLifetimeInMillis
        );
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie expiredAccessCookie = CookieUtil.deleteCookie(ACCESS_COOKIE_NAME);
        response.addHeader(HttpHeaders.SET_COOKIE, expiredAccessCookie.toString());

        ResponseCookie expiredRefreshCookie = CookieUtil.deleteCookie(REFRESH_COOKIE_NAME);
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshCookie.toString());
    }
}

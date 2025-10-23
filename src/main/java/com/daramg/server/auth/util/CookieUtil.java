package com.daramg.server.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public final class CookieUtil {

    public static ResponseCookie createCookie(final String cookieName, final String token,
                                              final Long cookieValidTimeMillis) {

        long maxAgeInSeconds = cookieValidTimeMillis / 1000L;
        return ResponseCookie.from(cookieName, token)
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(Math.toIntExact(maxAgeInSeconds))
                .httpOnly(true)
                .build();
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

    public static ResponseCookie deleteCookie(final String nameOfCookie) {
        return ResponseCookie.from(nameOfCookie, "")
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(0)
                .httpOnly(true)
                .build();
    }

}

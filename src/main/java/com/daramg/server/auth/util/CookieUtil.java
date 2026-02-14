package com.daramg.server.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public final class CookieUtil {

    public static ResponseCookie createCookie(final String cookieName, final String token,
                                              final Long cookieValidTimeMillis, final String domain) {

        long maxAgeInSeconds = cookieValidTimeMillis / 1000L;
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(Math.toIntExact(maxAgeInSeconds))
                .httpOnly(true);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        return builder.build();
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

    public static ResponseCookie deleteCookie(final String nameOfCookie, final String domain) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(nameOfCookie, "")
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(0)
                .httpOnly(true);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        return builder.build();
    }

}

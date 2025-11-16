package com.daramg.server.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("쿠키를 생성한다")
    void createCookie() {
        // given
        String cookieName = "testCookie";
        String token = "testToken";
        Long validTimeMillis = 3600000L;

        // when
        ResponseCookie responseCookie = CookieUtil.createCookie(cookieName, token, validTimeMillis);

        // then
        assertThat(responseCookie.getName()).isEqualTo(cookieName);
        assertThat(responseCookie.getValue()).isEqualTo(token);
        assertThat(responseCookie.getPath()).isEqualTo("/");
        assertThat(responseCookie.getSameSite()).isEqualTo("None");
        assertThat(responseCookie.isSecure()).isTrue();
        assertThat(responseCookie.isHttpOnly()).isTrue();
        assertThat(responseCookie.getMaxAge().getSeconds()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("쿠키가 존재할 때 쿠키를 찾는다")
    void getCookie_whenCookieExists() {
        // given
        String cookieName = "testCookie";
        Cookie cookie = new Cookie(cookieName, "testValue");
        Cookie[] cookies = {cookie};
        when(request.getCookies()).thenReturn(cookies);

        // when
        Optional<Cookie> result = CookieUtil.getCookie(request, cookieName);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(cookieName);
        assertThat(result.get().getValue()).isEqualTo("testValue");
    }

    @Test
    @DisplayName("쿠키가 존재하지 않을 때 빈 Optional을 반환한다")
    void getCookie_whenCookieNotExists() {
        // given
        String cookieName = "testCookie";
        Cookie[] cookies = {new Cookie("otherCookie", "value")};
        when(request.getCookies()).thenReturn(cookies);

        // when
        Optional<Cookie> result = CookieUtil.getCookie(request, cookieName);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("쿠키 배열이 null일 때 빈 Optional을 반환한다")
    void getCookie_whenCookiesIsNull() {
        // given
        String cookieName = "testCookie";
        when(request.getCookies()).thenReturn(null);

        // when
        Optional<Cookie> result = CookieUtil.getCookie(request, cookieName);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("쿠키를 삭제한다")
    void deleteCookie() {
        // given
        String cookieName = "testCookie";

        // when
        ResponseCookie responseCookie = CookieUtil.deleteCookie(cookieName);

        // then
        assertThat(responseCookie.getName()).isEqualTo(cookieName);
        assertThat(responseCookie.getValue()).isEmpty();
        assertThat(responseCookie.getPath()).isEqualTo("/");
        assertThat(responseCookie.getSameSite()).isEqualTo("Strict");
        assertThat(responseCookie.isSecure()).isTrue();
        assertThat(responseCookie.isHttpOnly()).isTrue();
        assertThat(responseCookie.getMaxAge().getSeconds()).isEqualTo(0L);
    }
}

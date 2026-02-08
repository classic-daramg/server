package com.daramg.server.notification.presentation;

import com.daramg.server.notification.application.NotificationService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
public class NotificationControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void 알림을_읽음_처리한다() throws Exception {
        // given
        Long notificationId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(patch("/notifications/{notificationId}/read", notificationId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notification API")
                                .summary("알림 읽음 처리")
                                .description("특정 알림을 읽음 상태로 변경합니다.")
                                .pathParameters(
                                        parameterWithName("notificationId").description("읽음 처리할 알림의 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 전체_알림을_읽음_처리한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(patch("/notifications/read-all")
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notification API")
                                .summary("전체 알림 읽음 처리")
                                .description("사용자의 모든 알림을 읽음 상태로 변경합니다.")
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 알림을_삭제한다() throws Exception {
        // given
        Long notificationId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(delete("/notifications/{notificationId}", notificationId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notification API")
                                .summary("알림 삭제")
                                .description("특정 알림을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("notificationId").description("삭제할 알림의 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

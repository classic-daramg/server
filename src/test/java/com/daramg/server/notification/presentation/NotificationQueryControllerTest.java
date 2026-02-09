package com.daramg.server.notification.presentation;

import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.notification.application.NotificationQueryService;
import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationQueryController.class)
public class NotificationQueryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
    void 알림_목록을_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        List<NotificationResponseDto> content = List.of(
                new NotificationResponseDto(
                        1L, "보내는사람", "profile.jpg", 10L, "게시글 제목",
                        NotificationType.COMMENT, false, LocalDateTime.of(2025, 1, 1, 12, 0)
                ),
                new NotificationResponseDto(
                        2L, "다른사람", null, 10L, "게시글 제목",
                        NotificationType.POST_LIKE, true, LocalDateTime.of(2025, 1, 1, 11, 0)
                )
        );
        PageResponseDto<NotificationResponseDto> response = new PageResponseDto<>(content, null, false);
        when(notificationQueryService.getNotifications(any(), any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/notifications")
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].senderNickname").value("보내는사람"))
                .andExpect(jsonPath("$.content[0].type").value("COMMENT"))
                .andExpect(jsonPath("$.content[0].isRead").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notification API")
                                .summary("알림 목록 조회")
                                .description("사용자의 알림 목록을 커서 기반 페이지네이션으로 조회합니다.")
                                .responseFields(
                                        fieldWithPath("content[].id").type(JsonFieldType.NUMBER).description("알림 아이디"),
                                        fieldWithPath("content[].senderNickname").type(JsonFieldType.STRING).description("알림 유발자 닉네임"),
                                        fieldWithPath("content[].senderProfileImage").type(JsonFieldType.STRING).description("알림 유발자 프로필 이미지").optional(),
                                        fieldWithPath("content[].postId").type(JsonFieldType.NUMBER).description("관련 게시글 아이디"),
                                        fieldWithPath("content[].postTitle").type(JsonFieldType.STRING).description("관련 게시글 제목"),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("알림 유형 (COMMENT, COMMENT_LIKE, POST_LIKE, REPLY)"),
                                        fieldWithPath("content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("알림 생성 시각"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 커서").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 안읽은_알림_수를_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        when(notificationQueryService.getUnreadCount(any())).thenReturn(5L);

        // when
        ResultActions result = mockMvc.perform(get("/notifications/unread-count")
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notification API")
                                .summary("안읽은 알림 수 조회")
                                .description("사용자의 안읽은 알림 개수를 조회합니다.")
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

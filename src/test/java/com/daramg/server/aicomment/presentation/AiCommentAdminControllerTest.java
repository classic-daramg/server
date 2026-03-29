package com.daramg.server.aicomment.presentation;

import com.daramg.server.aicomment.application.AiCommentService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiCommentAdminController.class)
public class AiCommentAdminControllerTest extends ControllerTestSupport {

    @MockitoBean
    private AiCommentService aiCommentService;

    @Test
    void 작곡가를_게시물에_수동_할당한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        doNothing().when(aiCommentService).scheduleManually(1L, 10L);

        // when
        ResultActions result = mockMvc.perform(post("/admin/ai-comments/posts/{postId}/assign", 1L)
                .queryParam("composerId", "10")
                .cookie(cookie));

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("AI Comment Admin API")
                                .summary("작곡가 수동 할당")
                                .description("특정 게시물에 작곡가를 직접 지정하여 AI 댓글 잡을 즉시 등록합니다. (ADMIN 전용)")
                                .pathParameters(
                                        parameterWithName("postId").description("댓글을 달 게시물 ID")
                                )
                                .queryParameters(
                                        parameterWithName("composerId").description("댓글을 달 작곡가 ID")
                                )
                                .build()
                        ),
                        requestCookies(cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰"))
                ));
    }

    @Test
    void 자동감지_설정을_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        when(aiCommentService.isAutoDetectEnabled()).thenReturn(true);

        // when
        ResultActions result = mockMvc.perform(get("/admin/ai-comments/settings")
                .cookie(cookie));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.autoDetectEnabled").value(true))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("AI Comment Admin API")
                                .summary("자동 감지 설정 조회")
                                .description("AI 댓글 자동 감지 활성화 여부를 조회합니다. (ADMIN 전용)")
                                .responseFields(
                                        fieldWithPath("autoDetectEnabled").type(BOOLEAN).description("자동 감지 활성화 여부")
                                )
                                .build()
                        ),
                        requestCookies(cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰"))
                ));
    }

    @Test
    void 자동감지_설정을_변경한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        doNothing().when(aiCommentService).setAutoDetectEnabled(false);

        // when
        ResultActions result = mockMvc.perform(put("/admin/ai-comments/settings/auto-detect")
                .queryParam("enabled", "false")
                .cookie(cookie));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("AI Comment Admin API")
                                .summary("자동 감지 설정 변경")
                                .description("AI 댓글 자동 감지 기능을 켜거나 끕니다. (ADMIN 전용)")
                                .queryParameters(
                                        parameterWithName("enabled").description("활성화 여부 (true/false)")
                                )
                                .build()
                        ),
                        requestCookies(cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰"))
                ));
    }
}

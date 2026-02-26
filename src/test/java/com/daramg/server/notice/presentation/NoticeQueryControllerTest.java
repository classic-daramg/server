package com.daramg.server.notice.presentation;

import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.notice.application.NoticeQueryService;
import com.daramg.server.notice.dto.NoticeDetailResponse;
import com.daramg.server.notice.dto.NoticeResponseDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NoticeQueryController.class)
public class NoticeQueryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NoticeQueryService noticeQueryService;

    @Test
    void 공지사항_목록을_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        List<NoticeResponseDto> notices = List.of(
                new NoticeResponseDto(1L, "첫 번째 공지", "관리자", Instant.now(), "공지 내용입니다.", "https://example.com/thumb1.jpg"),
                new NoticeResponseDto(2L, "두 번째 공지", "관리자", Instant.now(), "공지 내용입니다.", null)
        );
        PageResponseDto<NoticeResponseDto> response = new PageResponseDto<>(notices, "2", true);

        when(noticeQueryService.getAllPublishedNotices(any(), any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/notice")
                .param("cursor", "0")
                .param("size", "10")
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("첫 번째 공지"))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notice API")
                                .summary("공지사항 목록 조회")
                                .description("공지사항 목록을 커서 기반 페이지네이션으로 조회합니다.")
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("공지사항 목록"),
                                        fieldWithPath("content[].id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("공지사항 제목"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("작성일시"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("공지사항 내용"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 커서").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        ),
                        queryParameters(
                                parameterWithName("cursor").description("페이지 커서").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰").optional()
                        )
                ));
    }

    @Test
    void 공지사항_상세를_조회한다() throws Exception {
        // given
        Long noticeId = 1L;

        NoticeDetailResponse response = new NoticeDetailResponse(
                1L, "관리자", "https://example.com/profile.jpg",
                "공지사항 제목", "공지사항 상세 내용입니다.",
                List.of("https://example.com/image1.jpg"), Instant.now()
        );

        when(noticeQueryService.getNoticeDetail(anyLong())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/notice/{noticeId}", noticeId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("공지사항 제목"))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notice API")
                                .summary("공지사항 상세 조회")
                                .description("특정 공지사항의 상세 정보를 조회합니다.")
                                .pathParameters(
                                        parameterWithName("noticeId").description("조회할 공지사항 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                        fieldWithPath("writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("writerProfileImage").type(JsonFieldType.STRING).description("작성자 프로필 이미지"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("공지사항 제목"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("공지사항 내용"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("이미지 URL 목록"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING).description("작성일시")
                                )
                                .build()
                        )
                ));
    }
}

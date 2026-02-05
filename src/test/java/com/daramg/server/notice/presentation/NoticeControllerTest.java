package com.daramg.server.notice.presentation;

import com.daramg.server.notice.application.NoticeService;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.dto.NoticeUpdateDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NoticeController.class)
public class NoticeControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NoticeService noticeService;

    @Test
    void 공지사항을_생성한다() throws Exception {
        // given
        NoticeCreateDto requestDto = new NoticeCreateDto(
                "공지사항 제목",
                "공지사항 내용입니다. 5자 이상이어야 합니다.",
                List.of("https://example.com/image1.jpg")
        );
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/notice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notice API")
                                .summary("공지사항 생성")
                                .description("관리자가 공지사항을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("공지사항 제목 (최대 15자)"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("공지사항 내용 (5자 이상 3000자 이내)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 공지사항을_수정한다() throws Exception {
        // given
        Long noticeId = 1L;
        NoticeUpdateDto requestDto = new NoticeUpdateDto(
                "수정된 제목",
                "수정된 내용입니다. 5자 이상이어야 합니다.",
                List.of("https://example.com/new-image.jpg")
        );
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(patch("/notice/{noticeId}", noticeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notice API")
                                .summary("공지사항 수정")
                                .description("관리자가 공지사항을 수정합니다.")
                                .pathParameters(
                                        parameterWithName("noticeId").description("수정할 공지사항 ID")
                                )
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("공지사항 제목 (최대 15자)").optional(),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("공지사항 내용 (5자 이상 3000자 이내)").optional(),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 공지사항을_삭제한다() throws Exception {
        // given
        Long noticeId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(delete("/notice/{noticeId}", noticeId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Notice API")
                                .summary("공지사항 삭제")
                                .description("관리자가 공지사항을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("noticeId").description("삭제할 공지사항 ID")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

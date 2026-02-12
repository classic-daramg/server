package com.daramg.server.comment.presentation;

import com.daramg.server.comment.application.CommentService;
import com.daramg.server.comment.dto.CommentLikeResponseDto;
import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
public class CommentControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CommentService commentService;

    @Test
    void 댓글을_생성한다() throws Exception {
        // given
        Long postId = 1L;
        CommentCreateDto requestDto = new CommentCreateDto("댓글 내용입니다람쥐");
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/posts/{postId}/comments", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Comment API")
                                .summary("댓글 생성")
                                .description("사용자가 포스트에 댓글을 생성합니다.")
                                .pathParameters(
                                        parameterWithName("postId").description("댓글을 달 포스트의 아이디")
                                )
                                .requestFields(
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용(최대 500자)")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 대댓글을_생성한다() throws Exception {
        // given
        Long parentCommentId = 100L;
        CommentReplyCreateDto requestDto = new CommentReplyCreateDto("대댓글 내용입니다람쥐");
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/comments/{parentCommentId}/replies", parentCommentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Comment API")
                                .summary("대댓글 생성")
                                .description("사용자가 댓글에 대댓글을 생성합니다.")
                                .pathParameters(
                                        parameterWithName("parentCommentId").description("대댓글을 달 부모 댓글의 아이디")
                                )
                                .requestFields(
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("대댓글 내용(최대 500자)")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 비속어가_포함된_댓글_생성시_400을_반환한다() throws Exception {
        // given
        Long postId = 1L;
        CommentCreateDto requestDto = new CommentCreateDto("시발 이게 뭐야");
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/posts/{postId}/comments", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 비속어가_포함된_대댓글_생성시_400을_반환한다() throws Exception {
        // given
        Long parentCommentId = 100L;
        CommentReplyCreateDto requestDto = new CommentReplyCreateDto("병신 같은 글");
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/comments/{parentCommentId}/replies", parentCommentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 댓글을_삭제한다() throws Exception {
        // given
        Long commentId = 10L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(delete("/comments/{commentId}", commentId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Comment API")
                                .summary("댓글 삭제")
                                .description("사용자가 자신의 댓글을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("commentId").description("삭제할 댓글의 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 댓글_좋아요를_토글한다() throws Exception {
        // given
        Long commentId = 99L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        when(commentService.toggleCommentLike(anyLong(), any()))
                .thenReturn(new CommentLikeResponseDto(true, 7));

        // when
        ResultActions result = mockMvc.perform(post("/comments/{commentId}/like", commentId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.likeCount").value(7))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Comment API")
                                .summary("댓글 좋아요 토글")
                                .description("사용자가 댓글의 좋아요 상태를 토글합니다.")
                                .pathParameters(
                                        parameterWithName("commentId").description("대상 댓글의 아이디")
                                )
                                .responseFields(
                                        fieldWithPath("isLiked").type(JsonFieldType.BOOLEAN).description("현재 좋아요 여부"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("총 좋아요 수")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

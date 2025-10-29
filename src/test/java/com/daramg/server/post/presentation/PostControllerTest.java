package com.daramg.server.post.presentation;

import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.dto.PostCreateDto;
import com.daramg.server.post.dto.PostUpdateDto;
import com.daramg.server.post.application.PostService;
import com.daramg.server.post.presentation.PostController;
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

@WebMvcTest(controllers = PostController.class)
public class PostControllerTest extends ControllerTestSupport {

    @MockitoBean
    private PostService postService;

    @Test
    void 자유_포스트를_생성한다() throws Exception {
        //given
        PostCreateDto.CreateFree requestDto = new PostCreateDto.CreateFree(
                "포스트 제목", "포스트 내용입니다람쥐", PostStatus.PUBLISHED,
                List.of("image url 1", "image url 2"), "https://www.youtube.com/watch?v=mBXBOLG06Wc&list=RDmBXBOLG06Wc&start_radio=1",
                List.of("#seventeen", "#bss", "#fighting")
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        //when
        ResultActions result = mockMvc.perform(post("/posts/free")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isCreated()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("자유 포스트 생성")
                                .description("사용자가 자유 포스트를 생성합니다.")
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)"),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 큐레이션_포스트를_생성한다() throws Exception {
        //given
        PostCreateDto.CreateCuration requestDto = new PostCreateDto.CreateCuration(
                "큐레이션 포스트 제목", "큐레이션 포스트 내용입니다람쥐", PostStatus.DRAFT,
                List.of("image url 1", "image url 2"), "https://www.youtube.com/watch?v=mBXBOLG06Wc&list=RDmBXBOLG06Wc&start_radio=1",
                List.of("#classical", "#beethoven", "#music"),
                1L,
                List.of(2L, 3L)
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        //when
        ResultActions result = mockMvc.perform(post("/posts/curation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isCreated()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("큐레이션 포스트 생성")
                                .description("사용자가 큐레이션 포스트를 생성합니다.")
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)"),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트").optional(),
                                        fieldWithPath("primaryComposerId").type(JsonFieldType.NUMBER).description("주요 작곡가 아이디"),
                                        fieldWithPath("additionalComposerIds").type(JsonFieldType.ARRAY).description("추가 작곡가 아이디 리스트").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 스토리_포스트를_생성한다() throws Exception {
        //given
        PostCreateDto.CreateStory requestDto = new PostCreateDto.CreateStory(
                "스토리 포스트 제목", "스토리 포스트 내용입니다람쥐", PostStatus.PUBLISHED,
                List.of("image url 1", "image url 2"), "https://www.youtube.com/watch?v=mBXBOLG06Wc&list=RDmBXBOLG06Wc&start_radio=1",
                List.of("#romantic", "#chopin", "#piano"),
                1L
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        //when
        ResultActions result = mockMvc.perform(post("/posts/story")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isCreated()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("스토리 포스트 생성")
                                .description("사용자가 스토리 포스트를 생성합니다.")
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)"),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트").optional(),
                                        fieldWithPath("primaryComposerId").type(JsonFieldType.NUMBER).description("주요 작곡가 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 자유_포스트를_수정한다() throws Exception {
        //given
        Long postId = 1L;
        PostUpdateDto.UpdateFree requestDto = new PostUpdateDto.UpdateFree(
                "수정된 포스트 제목", "수정된 포스트 내용입니다람쥐", PostStatus.PUBLISHED,
                List.of("updated image url 1", "updated image url 2"), "https://www.youtube.com/watch?v=updated",
                List.of("#updated", "#seventeen", "#bss")
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "test-cookie");

        //when
        ResultActions result = mockMvc.perform(patch("/posts/free/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("자유 포스트 업데이트")
                                .description("사용자가 자유 포스트를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("postId").description("수정할 포스트의 아이디")
                                )
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)").optional(),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)").optional(),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트(삭제 시 빈 리스트)").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url(삭제 시 빈 문자열)").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트(삭제 시 빈 리스트)").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 스토리_포스트를_업데이트한다() throws Exception {
        //given
        Long postId = 1L;
        PostUpdateDto.UpdateStory requestDto = new PostUpdateDto.UpdateStory(
                "수정된 스토리 포스트 제목", "수정된 스토리 포스트 내용입니다람쥐", PostStatus.PUBLISHED,
                List.of("updated image url 1", "updated image url 2"), "https://www.youtube.com/watch?v=updated",
                List.of("#updated", "#story", "#chopin")
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "test-cookie");

        //when
        ResultActions result = mockMvc.perform(patch("/posts/story/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("스토리 포스트 업데이트")
                                .description("사용자가 스토리 포스트를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("postId").description("수정할 포스트의 아이디")
                                )
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)").optional(),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)").optional(),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트(삭제 시 빈 리스트)").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url(삭제 시 빈 문자열)").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트(삭제 시 빈 리스트)").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 큐레이션_포스트를_업데이트한다() throws Exception {
        //given
        Long postId = 1L;
        PostUpdateDto.UpdateCuration requestDto = new PostUpdateDto.UpdateCuration(
                "수정된 큐레이션 포스트 제목", "수정된 큐레이션 포스트 내용입니다람쥐", PostStatus.PUBLISHED,
                List.of("updated image url 1", "updated image url 2"), "https://www.youtube.com/watch?v=updated",
                List.of("#updated", "#curation", "#classical"),
                List.of(1L, 2L, 3L)
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "test-cookie");

        //when
        ResultActions result = mockMvc.perform(patch("/posts/curation/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("큐레이션 포스트 업데이트")
                                .description("사용자가 큐레이션 포스트를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("postId").description("수정할 포스트의 아이디")
                                )
                                .requestFields(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("포스트의 제목(15자 이내)").optional(),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("포스트의 내용(5자 이상 3000자 이하)").optional(),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING).description("포스트의 게시 상태(PUBLISHED, DRAFT)"),
                                        fieldWithPath("images").type(JsonFieldType.ARRAY).description("포스트에 넣을 이미지 리스트(삭제 시 빈 리스트)").optional(),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("포스트에 넣을 비디오 url(삭제 시 빈 문자열)").optional(),
                                        fieldWithPath("hashtags").type(JsonFieldType.ARRAY).description("포스트에 넣을 해시태그 리스트(삭제 시 빈 리스트)").optional(),
                                        fieldWithPath("additionalComposersId").type(JsonFieldType.ARRAY).description("추가 작곡가 ID 리스트(삭제 시 빈 리스트)").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 포스트를_삭제한다() throws Exception {
        //given
        Long postId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "test-cookie");

        //when
        ResultActions result = mockMvc.perform(delete("/posts/{postId}", postId)
                .cookie(cookie)
        );

        //then
        result.andExpect((status().isNoContent()))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post API")
                                .summary("포스트 삭제")
                                .description("사용자가 포스트를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("postId").description("삭제할 포스트의 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

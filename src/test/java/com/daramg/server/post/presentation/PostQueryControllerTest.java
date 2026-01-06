package com.daramg.server.post.presentation;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.post.application.PostQueryService;
import com.daramg.server.post.domain.PostType;
import com.daramg.server.post.dto.PostResponseDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostQueryController.class)
public class PostQueryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private PostQueryService postQueryService;

    @Test
    void 자유_포스트_목록을_조회한다() throws Exception {
        // given
        PostResponseDto freePost1 = new PostResponseDto(
                "자유 포스트 제목 1",
                "자유 포스트 내용입니다",
                List.of("#해시태그1", "#해시태그2"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                "작성자1",
                10,
                5,
                "https://example.com/image1.jpg",
                PostType.FREE
        );
        PostResponseDto freePost2 = new PostResponseDto(
                "자유 포스트 제목 2",
                "자유 포스트 내용입니다",
                List.of("#해시태그3"),
                LocalDateTime.of(2024, 1, 14, 15, 20, 0),
                "작성자2",
                20,
                10,
                null,
                PostType.FREE
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(freePost1, freePost2),
                "MjAyNC0wMS0xNVEyMDozMDowMF8xMjM=", // Base64 인코딩된 커서 (예시: "2024-01-15T10:30:00_123")
                true
        );

        when(postQueryService.getAllPublishedFreePosts(any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/free")
                .param("cursor", "MjAyNC0wMS0xNFQxNToyMDowMF80NTY=") // Base64 인코딩된 이전 커서 (예시)
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("자유_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("자유 포스트 목록 조회")
                                .description("자유 포스트 목록을 커서 기반 페이징으로 조회합니다. PUBLISHED 상태인 포스트만 조회되며, 생성일시 내림차순으로 정렬됩니다.")
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 큐레이션_포스트_목록을_조회한다() throws Exception {
        // given
        PostResponseDto curationPost1 = new PostResponseDto(
                "큐레이션 포스트 제목 1",
                "큐레이션 포스트 내용입니다",
                List.of("#큐레이션1", "#음악"),
                LocalDateTime.of(2024, 1, 15, 12, 0, 0),
                "작성자1",
                30,
                15,
                "https://example.com/curation1.jpg",
                PostType.CURATION
        );
        PostResponseDto curationPost2 = new PostResponseDto(
                "큐레이션 포스트 제목 2",
                "큐레이션 포스트 내용입니다",
                List.of("#큐레이션2"),
                LocalDateTime.of(2024, 1, 14, 18, 45, 0),
                "작성자2",
                25,
                12,
                null,
                PostType.CURATION
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(curationPost1, curationPost2),
                "MjAyNC0wMS0xNVEyMjowMDowMF8xMjM=", // Base64 인코딩된 커서 (예시: "2024-01-15T12:00:00_123")
                true
        );

        when(postQueryService.getAllPublishedCurationPosts(any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/curation")
                .param("cursor", "MjAyNC0wMS0xNFQxODo0NTowMF80NTY=") // Base64 인코딩된 이전 커서 (예시)
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("큐레이션_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("큐레이션 포스트 목록 조회")
                                .description("큐레이션 포스트 목록을 커서 기반 페이징으로 조회합니다. PUBLISHED 상태인 포스트만 조회되며, 생성일시 내림차순으로 정렬됩니다.")
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 스토리_포스트_목록을_조회한다() throws Exception {
        // given
        PostResponseDto storyPost1 = new PostResponseDto(
                "스토리 포스트 제목 1",
                "스토리 포스트 내용입니다",
                List.of("#스토리1", "#음악이야기"),
                LocalDateTime.of(2024, 1, 15, 14, 30, 0),
                "작성자1",
                50,
                25,
                "https://example.com/story1.jpg",
                PostType.STORY
        );
        PostResponseDto storyPost2 = new PostResponseDto(
                "스토리 포스트 제목 2",
                "스토리 포스트 내용입니다",
                List.of("#스토리2"),
                LocalDateTime.of(2024, 1, 14, 20, 15, 0),
                "작성자2",
                40,
                20,
                null,
                PostType.STORY
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(storyPost1, storyPost2),
                null,
                false
        );

        when(postQueryService.getAllPublishedStoryPosts(any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/story")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("스토리_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("스토리 포스트 목록 조회")
                                .description("스토리 포스트 목록을 커서 기반 페이징으로 조회합니다. PUBLISHED 상태인 포스트만 조회되며, 생성일시 내림차순으로 정렬됩니다.")
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 유저가_작성한_발행된_포스트_목록을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        PostResponseDto publishedPost1 = new PostResponseDto(
                "발행된 포스트 제목 1",
                "발행된 포스트 내용입니다",
                List.of("#발행1", "#음악"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                "작성자1",
                15,
                8,
                "https://example.com/published1.jpg",
                PostType.FREE
        );
        PostResponseDto publishedPost2 = new PostResponseDto(
                "발행된 포스트 제목 2",
                "발행된 포스트 내용입니다",
                List.of("#발행2"),
                LocalDateTime.of(2024, 1, 14, 15, 20, 0),
                "작성자1",
                20,
                10,
                null,
                PostType.STORY
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(publishedPost1, publishedPost2),
                "MjAyNC0wMS0xNVEyMDozMDowMF8xMjM=",
                true
        );

        when(postQueryService.getUserPublishedPosts(eq(userId), any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/{userId}/published", userId)
                .param("cursor", "MjAyNC0wMS0xNFQxNToyMDowMF80NTY=")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("유저_발행_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("유저가 작성한 발행된 포스트 목록 조회")
                                .description("특정 유저가 작성한 포스트 중 PUBLISHED 상태인 포스트 목록을 커서 기반 페이징으로 조회합니다. 생성일시 내림차순으로 정렬됩니다.")
                                .pathParameters(
                                        parameterWithName("userId").description("조회할 유저 ID")
                                )
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 유저가_작성한_임시저장_포스트_목록을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        PostResponseDto draftPost1 = new PostResponseDto(
                "임시저장 포스트 제목 1",
                "임시저장 포스트 내용입니다",
                List.of("#임시저장1", "#작업중"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                "작성자1",
                0,
                0,
                "https://example.com/draft1.jpg",
                PostType.CURATION
        );
        PostResponseDto draftPost2 = new PostResponseDto(
                "임시저장 포스트 제목 2",
                "임시저장 포스트 내용입니다",
                List.of("#임시저장2"),
                LocalDateTime.of(2024, 1, 14, 15, 20, 0),
                "작성자1",
                0,
                0,
                null,
                PostType.FREE
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(draftPost1, draftPost2),
                "MjAyNC0wMS0xNVEyMDozMDowMF8xMjM=",
                true
        );

        when(postQueryService.getUserDraftPosts(eq(userId), any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/{userId}/drafts", userId)
                .param("cursor", "MjAyNC0wMS0xNFQxNToyMDowMF80NTY=")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("유저_임시저장_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("유저가 작성한 임시저장 포스트 목록 조회")
                                .description("특정 유저가 작성한 포스트 중 DRAFT 상태인 포스트 목록을 커서 기반 페이징으로 조회합니다. 생성일시 내림차순으로 정렬됩니다.")
                                .pathParameters(
                                        parameterWithName("userId").description("조회할 유저 ID")
                                )
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 유저가_스크랩한_포스트_목록을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        PostResponseDto scrappedPost1 = new PostResponseDto(
                "스크랩한 포스트 제목 1",
                "스크랩한 포스트 내용입니다",
                List.of("#스크랩1", "#좋은음악"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                "다른작성자1",
                50,
                25,
                "https://example.com/scrap1.jpg",
                PostType.STORY
        );
        PostResponseDto scrappedPost2 = new PostResponseDto(
                "스크랩한 포스트 제목 2",
                "스크랩한 포스트 내용입니다",
                List.of("#스크랩2"),
                LocalDateTime.of(2024, 1, 14, 15, 20, 0),
                "다른작성자2",
                30,
                15,
                null,
                PostType.CURATION
        );

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>(
                List.of(scrappedPost1, scrappedPost2),
                null,
                false
        );

        when(postQueryService.getUserScrappedPosts(eq(userId), any(PageRequestDto.class))).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/posts/{userId}/scraps", userId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("유저_스크랩_포스트_목록_조회",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Post Query API")
                                .summary("유저가 스크랩한 포스트 목록 조회")
                                .description("특정 유저가 스크랩한 포스트 목록을 커서 기반 페이징으로 조회합니다. PUBLISHED 상태인 포스트만 조회되며, 생성일시 내림차순으로 정렬됩니다.")
                                .pathParameters(
                                        parameterWithName("userId").description("조회할 유저 ID")
                                )
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }
}


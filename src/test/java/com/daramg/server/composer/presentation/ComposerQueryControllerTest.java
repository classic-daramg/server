package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerQueryService;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.composer.dto.ComposerWithPostsResponseDto;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.post.application.PostQueryService;
import com.daramg.server.post.dto.PostResponseDto;
import com.daramg.server.post.domain.PostType;
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

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ComposerQueryController.class)
public class ComposerQueryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ComposerQueryService composerQueryService;

    @MockitoBean
    private PostQueryService postQueryService;

    @Test
    void 작곡가_목록을_조회한다() throws Exception {
        // given
        List<ComposerResponseDto> response = List.of(
                new ComposerResponseDto(1L, "코렐리", "Arcangelo Corelli", "Arcangelo Corelli", "이탈리아", "MALE", (short) 1653, (short) 1713, "“바이올린의 따스한 숨결이 서로를 감싸는 밤”", true),
                new ComposerResponseDto(2L, "비탈리", "Tomaso Antonio Vitali", "Tomaso Antonio Vitali", "이탈리아", "MALE", (short) 1663, (short) 1745, "“서정이 흐르는 현의 떨림, 조용히 마음을 울리는 음악”", false),
                new ComposerResponseDto(3L, "A. 스카를라티", "Alessandro Scarlatti", "Alessandro Scarlatti", "이탈리아", "MALE", (short) 1660, (short) 1725, "“수많은 이야기 속에 피어나는 이탈리아의 정열”", true),
                new ComposerResponseDto(4L, "D. 스카를라티", "Domenico Scarlatti", "Domenico Scarlatti", "이탈리아", "MALE", (short) 1685, (short) 1757, "“하늘을 나는 건반 위 상상, 자유로운 영혼의 소나타”", false),
                new ComposerResponseDto(5L, "비발디", "Antonio Vivaldi", "Antonio Vivaldi", "이탈리아", "MALE", (short) 1678, (short) 1741, "“빨간 머리의 계절처럼 쏟아지는 생명과 빛”", true),
                new ComposerResponseDto(6L, "타르티니", "Giuseppe Tartini", "Giuseppe Tartini", "이탈리아", "MALE", (short) 1692, (short) 1770, "“악마도 울릴 만큼 깊은 꿈결, 신비로운 선율의 마법”", false),
                new ComposerResponseDto(7L, "파헬벨", "Johann Pachelbel", "Johann Pachelbel", "독일", "MALE", (short) 1653, (short) 1706, "“시간 너머의 따뜻한 안식, 평화로운 하루의 시작”", true),
                new ComposerResponseDto(8L, "마테존", "Johann Mattheson", "Johann Mattheson", "독일", "MALE", (short) 1681, (short) 1764, "“생각과 음악이 나란히 걷는 길, 새로움을 질문하는 순간”", false)
        );
        when(composerQueryService.getAllComposers(any(), any(), any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/composers")
                .param("eras", "BAROQUE", "CLASSICAL")
                .param("continents", "EUROPE")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 목록 조회")
                                .description("로그인 여부에 따라 isLiked 여부가 포함되며 era, continent 리스트로 필터링합니다.")
                                .queryParameters(
                                        parameterWithName("eras").description("필터링할 시대(Era) 목록: MEDIEVAL_RENAISSANCE, BAROQUE, CLASSICAL, ROMANTIC, MODERN_CONTEMPORARY").optional(),
                                        parameterWithName("continents").description("필터링할 대륙(Continent) 목록: ASIA, NORTH_AMERICA, EUROPE, SOUTH_AMERICA, AFRICA_OCEANIA").optional()
                                )
                                .responseFields(
                                        fieldWithPath("[].composerId").type(JsonFieldType.NUMBER).description("작곡가 ID"),
                                        fieldWithPath("[].koreanName").type(JsonFieldType.STRING).description("작곡가 한글 이름"),
                                        fieldWithPath("[].englishName").type(JsonFieldType.STRING).description("작곡가 영어 이름"),
                                        fieldWithPath("[].nativeName").type(JsonFieldType.STRING).description("작곡가 원어 이름").optional(),
                                        fieldWithPath("[].nationality").type(JsonFieldType.STRING).description("작곡가 국적").optional(),
                                        fieldWithPath("[].gender").type(JsonFieldType.STRING).description("작곡가 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("[].birthYear").type(JsonFieldType.NUMBER).description("작곡가 출생년도").optional(),
                                        fieldWithPath("[].deathYear").type(JsonFieldType.NUMBER).description("작곡가 사망년도").optional(),
                                        fieldWithPath("[].bio").type(JsonFieldType.STRING).description("작곡가 소개").optional(),
                                        fieldWithPath("[].isLiked").type(JsonFieldType.BOOLEAN).description("현재 유저의 좋아요 여부 (비로그인 시 false)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 작곡가_정보와_포스트_목록을_조회한다() throws Exception {
        // given
        Long composerId = 1L;
        ComposerResponseDto composerDto = new ComposerResponseDto(
                1L, "코렐리", "Arcangelo Corelli", "Arcangelo Corelli", "이탈리아", "MALE",
                (short) 1653, (short) 1713, "“바이올린의 따스한 숨결이 서로를 감싸는 밤”", true
        );

        PostResponseDto post1 = new PostResponseDto(
                1L,
                "코렐리 스토리 포스트",
                "코렐리에 대한 스토리 포스트 내용입니다",
                List.of("#코렐리", "#바로크"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                "작성자1",
                10,
                5,
                "https://example.com/image1.jpg",
                PostType.STORY,
                true,
                false
        );

        PostResponseDto post2 = new PostResponseDto(
                2L,
                "코렐리 큐레이션 포스트",
                "코렐리에 대한 큐레이션 포스트 내용입니다",
                List.of("#코렐리", "#큐레이션"),
                LocalDateTime.of(2024, 1, 14, 15, 20, 0),
                "작성자2",
                20,
                10,
                null,
                PostType.CURATION,
                false,
                true
        );

        PageResponseDto<PostResponseDto> postsPage = new PageResponseDto<>(
                List.of(post1, post2),
                "MjAyNC0wMS0xNVEyMDozMDowMF8xMjM=",
                true
        );

        ComposerWithPostsResponseDto response = new ComposerWithPostsResponseDto(composerDto, postsPage);

        when(postQueryService.getComposerWithPosts(eq(composerId), any(PageRequestDto.class), any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/composers/{composerId}/posts", composerId)
                .param("cursor", "MjAyNC0wMS0xNFQxNToyMDowMF80NTY=")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 정보와 포스트 목록 조회")
                                .description("작곡가 ID로 작곡가 정보와 해당 작곡가가 primaryComposer로 연결된 STORY, CURATION 타입 포스트 목록을 커서 기반 페이징으로 조회합니다.")
                                .pathParameters(
                                        parameterWithName("composerId").description("조회할 작곡가 ID")
                                )
                                .queryParameters(
                                        parameterWithName("cursor").description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 첫 페이지 조회 시 생략 가능 (기본값: null)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에 조회할 포스트 개수. 생략 시 기본값: 10")
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("composer").type(JsonFieldType.OBJECT).description("작곡가 정보"),
                                        fieldWithPath("composer.composerId").type(JsonFieldType.NUMBER).description("작곡가 ID"),
                                        fieldWithPath("composer.koreanName").type(JsonFieldType.STRING).description("작곡가 한글 이름"),
                                        fieldWithPath("composer.englishName").type(JsonFieldType.STRING).description("작곡가 영어 이름"),
                                        fieldWithPath("composer.nativeName").type(JsonFieldType.STRING).description("작곡가 원어 이름").optional(),
                                        fieldWithPath("composer.nationality").type(JsonFieldType.STRING).description("작곡가 국적").optional(),
                                        fieldWithPath("composer.gender").type(JsonFieldType.STRING).description("작곡가 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("composer.birthYear").type(JsonFieldType.NUMBER).description("작곡가 출생년도").optional(),
                                        fieldWithPath("composer.deathYear").type(JsonFieldType.NUMBER).description("작곡가 사망년도").optional(),
                                        fieldWithPath("composer.bio").type(JsonFieldType.STRING).description("작곡가 소개").optional(),
                                        fieldWithPath("composer.isLiked").type(JsonFieldType.BOOLEAN).description("현재 유저의 좋아요 여부 (비로그인 시 false)"),
                                        fieldWithPath("posts").type(JsonFieldType.OBJECT).description("포스트 목록 페이징 정보"),
                                        fieldWithPath("posts.content").type(JsonFieldType.ARRAY).description("포스트 목록"),
                                        fieldWithPath("posts.content[].id").type(JsonFieldType.NUMBER).description("포스트 ID"),
                                        fieldWithPath("posts.content[].title").type(JsonFieldType.STRING).description("포스트 제목"),
                                        fieldWithPath("posts.content[].content").type(JsonFieldType.STRING).description("포스트 내용"),
                                        fieldWithPath("posts.content[].hashtags").type(JsonFieldType.ARRAY).description("해시태그 목록"),
                                        fieldWithPath("posts.content[].createdAt").type(JsonFieldType.STRING).description("생성일시 (ISO 8601 형식)"),
                                        fieldWithPath("posts.content[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("posts.content[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("posts.content[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("posts.content[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                        fieldWithPath("posts.content[].type").type(JsonFieldType.STRING).description("포스트 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("posts.content[].isLiked").type(JsonFieldType.BOOLEAN).description("로그인한 유저의 좋아요 여부 (비로그인 시 null)").optional(),
                                        fieldWithPath("posts.content[].isScrapped").type(JsonFieldType.BOOLEAN).description("로그인한 유저의 스크랩 여부 (비로그인 시 null)").optional(),
                                        fieldWithPath("posts.nextCursor").type(JsonFieldType.STRING).description("다음 페이지 조회를 위한 커서 (Base64 인코딩된 문자열). 마지막 페이지인 경우 null").optional(),
                                        fieldWithPath("posts.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }
}

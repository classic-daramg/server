package com.daramg.server.search.presentation;

import com.daramg.server.post.domain.PostType;
import com.daramg.server.search.application.SearchService;
import com.daramg.server.search.dto.SearchResponseDto;
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

@WebMvcTest(SearchController.class)
public class SearchControllerTest extends ControllerTestSupport {

    @MockitoBean
    private SearchService searchService;

    @Test
    void 키워드로_작곡가와_게시글을_검색한다() throws Exception {
        // given
        String keyword = "모차르트";

        SearchResponseDto.ComposerResult composer = new SearchResponseDto.ComposerResult(
                1L,
                "볼프강 아마데우스 모차르트",
                "Wolfgang Amadeus Mozart"
        );

        SearchResponseDto.PostResult post = new SearchResponseDto.PostResult(
                10L,
                "모차르트의 피아노 협주곡",
                PostType.STORY,
                "작성자닉네임",
                LocalDateTime.of(2024, 3, 1, 12, 0, 0)
        );

        SearchResponseDto response = new SearchResponseDto(List.of(composer), List.of(post));

        when(searchService.search(eq(keyword), any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/search")
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(document("검색",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("키워드 검색")
                                .description("키워드로 작곡가와 게시글을 검색하고, 검색 기록을 저장합니다.")
                                .queryParameters(
                                        parameterWithName("keyword").description("검색 키워드")
                                )
                                .responseFields(
                                        fieldWithPath("composers").type(JsonFieldType.ARRAY).description("검색된 작곡가 목록"),
                                        fieldWithPath("composers[].id").type(JsonFieldType.NUMBER).description("작곡가 ID"),
                                        fieldWithPath("composers[].koreanName").type(JsonFieldType.STRING).description("작곡가 한국어 이름"),
                                        fieldWithPath("composers[].englishName").type(JsonFieldType.STRING).description("작곡가 영어 이름"),
                                        fieldWithPath("posts").type(JsonFieldType.ARRAY).description("검색된 게시글 목록"),
                                        fieldWithPath("posts[].id").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                        fieldWithPath("posts[].title").type(JsonFieldType.STRING).description("게시글 제목"),
                                        fieldWithPath("posts[].type").type(JsonFieldType.STRING).description("게시글 타입 (FREE, CURATION, STORY)"),
                                        fieldWithPath("posts[].writerNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                        fieldWithPath("posts[].createdAt").type(JsonFieldType.STRING).description("게시글 작성 시각")
                                )
                                .build()
                        )
                ));
    }
}

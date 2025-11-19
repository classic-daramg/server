package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerQueryService;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ComposerQueryController.class)
public class ComposerQueryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ComposerQueryService composerQueryService;

    @Test
    void 작곡가_목록을_조회한다() throws Exception {
        // given
        List<ComposerResponseDto> response = List.of(
                new ComposerResponseDto(1L, "코렐리", "“바이올린의 따스한 숨결이 서로를 감싸는 밤”", true),
                new ComposerResponseDto(2L, "비탈리", "“서정이 흐르는 현의 떨림, 조용히 마음을 울리는 음악”", false),
                new ComposerResponseDto(3L, "A. 스카를라티", "“수많은 이야기 속에 피어나는 이탈리아의 정열”", true),
                new ComposerResponseDto(4L, "D. 스카를라티", "“하늘을 나는 건반 위 상상, 자유로운 영혼의 소나타”", false),
                new ComposerResponseDto(5L, "비발디", "“빨간 머리의 계절처럼 쏟아지는 생명과 빛”", true),
                new ComposerResponseDto(6L, "타르티니", "“악마도 울릴 만큼 깊은 꿈결, 신비로운 선율의 마법”", false),
                new ComposerResponseDto(7L, "파헬벨", "“시간 너머의 따뜻한 안식, 평화로운 하루의 시작”", true),
                new ComposerResponseDto(8L, "마테존", "“생각과 음악이 나란히 걷는 길, 새로움을 질문하는 순간”", false)
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
                                        fieldWithPath("[].bio").type(JsonFieldType.STRING).description("작곡가 소개").optional(),
                                        fieldWithPath("[].isLiked").type(JsonFieldType.BOOLEAN).description("현재 유저의 좋아요 여부 (비로그인 시 false)")
                                )
                                .build()
                        )
                ));
    }
}

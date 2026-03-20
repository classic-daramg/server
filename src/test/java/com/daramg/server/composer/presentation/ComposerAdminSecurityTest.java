package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerService;
import com.daramg.server.testsupport.support.SecurityControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ComposerController.class)
public class ComposerAdminSecurityTest extends SecurityControllerTestSupport {

    @MockitoBean
    private ComposerService composerService;

    // ===== ADMIN 성공 케이스 =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_작곡가를_생성할_수_있다() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "koreanName", "베토벤",
                "englishName", "Ludwig van Beethoven",
                "gender", "MALE",
                "nationality", "독일",
                "birthYear", 1770,
                "deathYear", 1827,
                "bio", "독일의 작곡가",
                "era", "CLASSICAL",
                "continent", "EUROPE"
        );

        ResultActions result = mockMvc.perform(post("/composers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        );

        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer Admin API")
                                .summary("작곡가 생성 (ADMIN 전용)")
                                .description("ADMIN 권한을 가진 유저만 작곡가를 생성할 수 있습니다. 일반 유저는 403을 반환합니다.")
                                .requestFields(
                                        fieldWithPath("koreanName").type(JsonFieldType.STRING).description("한국어 이름"),
                                        fieldWithPath("englishName").type(JsonFieldType.STRING).description("영어 이름"),
                                        fieldWithPath("gender").type(JsonFieldType.STRING).description("성별 (MALE / FEMALE / UNKNOWN)"),
                                        fieldWithPath("nationality").type(JsonFieldType.STRING).description("국적").optional(),
                                        fieldWithPath("birthYear").type(JsonFieldType.NUMBER).description("출생 연도").optional(),
                                        fieldWithPath("deathYear").type(JsonFieldType.NUMBER).description("사망 연도").optional(),
                                        fieldWithPath("bio").type(JsonFieldType.STRING).description("소개").optional(),
                                        fieldWithPath("era").type(JsonFieldType.STRING).description("시대 (MEDIEVAL_RENAISSANCE / BAROQUE / CLASSICAL / ROMANTIC / MODERN_CONTEMPORARY)").optional(),
                                        fieldWithPath("continent").type(JsonFieldType.STRING).description("대륙 (ASIA / NORTH_AMERICA / EUROPE / SOUTH_AMERICA / AFRICA_OCEANIA)").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_작곡가를_수정할_수_있다() throws Exception {
        Long composerId = 1L;
        Map<String, Object> requestBody = Map.of(
                "koreanName", "베토벤",
                "englishName", "Ludwig van Beethoven",
                "gender", "MALE",
                "nationality", "독일",
                "birthYear", 1770,
                "deathYear", 1827,
                "bio", "독일의 작곡가",
                "era", "CLASSICAL",
                "continent", "EUROPE"
        );

        ResultActions result = mockMvc.perform(put("/composers/{composerId}", composerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        );

        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer Admin API")
                                .summary("작곡가 수정 (ADMIN 전용)")
                                .description("ADMIN 권한을 가진 유저만 작곡가 정보를 수정할 수 있습니다. 일반 유저는 403을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("composerId").description("수정할 작곡가 ID")
                                )
                                .requestFields(
                                        fieldWithPath("koreanName").type(JsonFieldType.STRING).description("한국어 이름"),
                                        fieldWithPath("englishName").type(JsonFieldType.STRING).description("영어 이름"),
                                        fieldWithPath("nativeName").type(JsonFieldType.STRING).description("원어 이름").optional(),
                                        fieldWithPath("gender").type(JsonFieldType.STRING).description("성별 (MALE / FEMALE / UNKNOWN)"),
                                        fieldWithPath("nationality").type(JsonFieldType.STRING).description("국적"),
                                        fieldWithPath("birthYear").type(JsonFieldType.NUMBER).description("출생 연도"),
                                        fieldWithPath("deathYear").type(JsonFieldType.NUMBER).description("사망 연도"),
                                        fieldWithPath("bio").type(JsonFieldType.STRING).description("소개"),
                                        fieldWithPath("era").type(JsonFieldType.STRING).description("시대 (MEDIEVAL_RENAISSANCE / BAROQUE / CLASSICAL / ROMANTIC / MODERN_CONTEMPORARY)"),
                                        fieldWithPath("continent").type(JsonFieldType.STRING).description("대륙 (ASIA / NORTH_AMERICA / EUROPE / SOUTH_AMERICA / AFRICA_OCEANIA)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_작곡가를_삭제할_수_있다() throws Exception {
        Long composerId = 1L;

        ResultActions result = mockMvc.perform(delete("/composers/{composerId}", composerId));

        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer Admin API")
                                .summary("작곡가 삭제 (ADMIN 전용)")
                                .description("ADMIN 권한을 가진 유저만 작곡가를 삭제할 수 있습니다. 일반 유저는 403을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("composerId").description("삭제할 작곡가 ID")
                                )
                                .build()
                        )
                ));
    }

    // ===== USER 403 케이스 =====

    @Test
    @WithMockUser(roles = "USER")
    void 일반_유저는_작곡가_생성_시_403을_반환한다() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "koreanName", "베토벤",
                "englishName", "Ludwig van Beethoven",
                "gender", "MALE"
        );

        mockMvc.perform(post("/composers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_유저는_작곡가_수정_시_403을_반환한다() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "koreanName", "베토벤",
                "englishName", "Ludwig van Beethoven",
                "gender", "MALE",
                "nationality", "독일",
                "birthYear", 1770,
                "deathYear", 1827,
                "bio", "독일의 작곡가",
                "era", "CLASSICAL",
                "continent", "EUROPE"
        );

        mockMvc.perform(put("/composers/{composerId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_유저는_작곡가_삭제_시_403을_반환한다() throws Exception {
        mockMvc.perform(delete("/composers/{composerId}", 1L))
                .andExpect(status().isForbidden());
    }
}

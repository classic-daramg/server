package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ComposerController.class)
public class ComposerControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ComposerService composerService;

    @Test
    void 작곡가를_생성한다() throws Exception {
        // given
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
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/composers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 생성")
                                .description("관리자가 작곡가를 생성합니다.")
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
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 작곡가를_수정한다() throws Exception {
        // given
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
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(put("/composers/{composerId}", composerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 수정")
                                .description("관리자가 작곡가 정보를 수정합니다.")
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
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 작곡가를_삭제한다() throws Exception {
        // given
        Long composerId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(delete("/composers/{composerId}", composerId)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 삭제")
                                .description("관리자가 작곡가를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("composerId").description("삭제할 작곡가 ID")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 작곡가_좋아요를_토글한다() throws Exception {
        // given
        Long composerId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "test-cookie");

        // when
        ResultActions result = mockMvc.perform(post("/composers/{composerId}/like", composerId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Composer API")
                                .summary("작곡가 좋아요 토글")
                                .description("유저가 작곡가의 좋아요 상태를 토글합니다.")
                                .pathParameters(
                                        parameterWithName("composerId").description("대상 작곡가의 아이디")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

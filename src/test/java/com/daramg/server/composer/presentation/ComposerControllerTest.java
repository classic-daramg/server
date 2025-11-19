package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ComposerController.class)
public class ComposerControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ComposerService composerService;

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

package com.daramg.server.admin.presentation;

import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
public class AdminControllerTest extends ControllerTestSupport {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String ADMIN_BACKUP_COOKIE_NAME = "admin_backup";

    private User createTestUser(Long id) {
        User user = User.builder()
                .email("test@squirrel.cl")
                .password("encodedPassword")
                .name("테스트유저")
                .birthDate(LocalDate.of(1999, 1, 1))
                .nickname("테스트닉네임")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void 어드민이_특정_유저_계정으로_전환() throws Exception {
        // given
        User targetUser = createTestUser(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(targetUser));
        when(jwtUtil.createAccessToken(any(User.class))).thenReturn("impersonated-token");

        // when
        ResultActions result = mockMvc.perform(post("/admin/impersonate/{userId}", 10L)
                .cookie(new Cookie(COOKIE_NAME, "admin-access-token")));

        // then
        result.andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, "impersonated-token"))
                .andExpect(cookie().value(ADMIN_BACKUP_COOKIE_NAME, "admin-access-token"))
                .andDo(document("어드민_계정_전환",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin API")
                                .summary("계정 전환")
                                .description("어드민이 특정 유저 계정으로 전환합니다. 기존 어드민 토큰은 admin_backup 쿠키에 저장됩니다.")
                                .build()
                        ),
                        pathParameters(
                                parameterWithName("userId").description("전환할 유저 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("어드민 액세스 토큰")
                        ),
                        responseCookies(
                                cookieWithName(COOKIE_NAME).description("전환된 유저의 액세스 토큰"),
                                cookieWithName(ADMIN_BACKUP_COOKIE_NAME).description("백업된 어드민 액세스 토큰")
                        )
                ));
    }

    @Test
    void 존재하지_않는_유저로_전환_시_실패() throws Exception {
        // given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // when
        ResultActions result = mockMvc.perform(post("/admin/impersonate/{userId}", 999L)
                .cookie(new Cookie(COOKIE_NAME, "admin-access-token")));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 어드민_계정으로_복귀() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/admin/revert")
                .cookie(new Cookie(COOKIE_NAME, "impersonated-token"))
                .cookie(new Cookie(ADMIN_BACKUP_COOKIE_NAME, "admin-access-token")));

        // then
        result.andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, "admin-access-token"))
                .andExpect(cookie().value(ADMIN_BACKUP_COOKIE_NAME, ""))
                .andDo(document("어드민_계정_복귀",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin API")
                                .summary("어드민 계정 복귀")
                                .description("백업된 어드민 토큰으로 복귀합니다. admin_backup 쿠키가 삭제되고 어드민 액세스 토큰이 복원됩니다.")
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("현재 전환된 유저의 액세스 토큰"),
                                cookieWithName(ADMIN_BACKUP_COOKIE_NAME).description("백업된 어드민 액세스 토큰")
                        ),
                        responseCookies(
                                cookieWithName(COOKIE_NAME).description("복원된 어드민 액세스 토큰"),
                                cookieWithName(ADMIN_BACKUP_COOKIE_NAME).description("삭제된 백업 쿠키")
                        )
                ));
    }

    @Test
    void 백업_쿠키_없이_복귀_시_실패() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/admin/revert")
                .cookie(new Cookie(COOKIE_NAME, "impersonated-token")));

        // then
        result.andExpect(status().isUnauthorized());
    }
}

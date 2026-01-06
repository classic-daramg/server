package com.daramg.server.user.presentation;

import com.daramg.server.user.application.UserService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.dto.UserProfileResponseDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest extends ControllerTestSupport {

    @MockitoBean
    private UserService userService;

    @Test
    void 닉네임_중복_확인_사용가능() throws Exception {
        // given
        String nickname = "햄쥑이";
        given(userService.isNicknameAvailable(nickname)).willReturn(true);

        // when
        ResultActions result = mockMvc.perform(get("/users/check-nickname")
                .param("nickname", nickname)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("User API")
                                .summary("닉네임 중복 확인")
                                .description("사용자가 입력한 닉네임의 중복 여부를 확인합니다.")
                                .queryParameters(
                                        parameterWithName("nickname").description("중복 확인할 닉네임")
                                )
                                .responseFields(
                                        fieldWithPath("닉네임 사용 가능 유무: ").type(JsonFieldType.BOOLEAN).description("닉네임 사용 가능 여부 (true: 사용 가능, false: 사용 불가능)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 유저_프로필을_조회한다() throws Exception {
        // given
        String profileImage = "https://example.com/profile.jpg";
        String nickname = "테스트닉네임";
        String bio = "테스트 소개글";
        UserProfileResponseDto responseDto = new UserProfileResponseDto(profileImage, nickname, bio);
        given(userService.getProfile(any(User.class))).willReturn(responseDto);

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("User API")
                                .summary("유저 프로필 조회")
                                .description("현재 로그인한 유저의 프로필 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("profileImage").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("bio").type(JsonFieldType.STRING).description("자기소개").optional()
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 유저를_팔로우한다() throws Exception {
        // given
        Long followedId = 1L;
        willDoNothing().given(userService).follow(any(User.class), eq(followedId));

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(post("/users/following/{followedId}", followedId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("User API")
                                .summary("유저 팔로우")
                                .description("특정 유저를 팔로우합니다.")
                                .pathParameters(
                                        parameterWithName("followedId").description("팔로우할 유저의 ID")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }

    @Test
    void 유저를_언팔로우한다() throws Exception {
        // given
        Long followedId = 1L;
        willDoNothing().given(userService).unfollow(any(User.class), eq(followedId));

        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");

        // when
        ResultActions result = mockMvc.perform(delete("/users/unfollowing/{followedId}", followedId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("User API")
                                .summary("유저 언팔로우")
                                .description("특정 유저를 언팔로우합니다.")
                                .pathParameters(
                                        parameterWithName("followedId").description("언팔로우할 유저의 ID")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("유저의 토큰")
                        )
                ));
    }
}

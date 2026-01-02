package com.daramg.server.auth.presentation;

import com.daramg.server.auth.domain.EmailPurpose;
import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.application.AuthService;
import com.daramg.server.auth.dto.*;
import com.daramg.server.user.domain.User;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends ControllerTestSupport {

    @Value("${cookie.access-name}")
    public String ACCESS_COOKIE_NAME;

    @Value("${cookie.refresh-name}")
    public String REFRESH_COOKIE_NAME;

    @MockitoBean
    private MailVerificationService mailVerificationService;
    
    @MockitoBean
    private AuthService authService;

    @Test
    void 회원가입을_위한_인증코드_메일_발송() throws Exception {
        // given
        EmailVerificationRequestDto request = new EmailVerificationRequestDto("daramg123@gmail.com", EmailPurpose.SIGNUP);

        doNothing().when(mailVerificationService).sendVerificationEmail(any(EmailVerificationRequestDto.class));

        // when
        ResultActions result =  mockMvc.perform(post("/auth/email-verifications", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(document("회원가입을_위한_인증코드_메일_발송",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("인증코드 이메일 발송")
                                .description("이메일 주소로 인증코드를 발송합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("emailPurpose").type(JsonFieldType.STRING).description("이메일 인증코드 전송 목적(SIGNUP, PASSWORD_RESET)")
                                )
                                .build()
                        )
                ));

    }

    @Test
    void 비밀번호_초기화를_위한_인증코드_메일_발송() throws Exception {
        // given
        EmailVerificationRequestDto request = new EmailVerificationRequestDto("daramg123@gmail.com", EmailPurpose.PASSWORD_RESET);

        doNothing().when(mailVerificationService).sendVerificationEmail(any(EmailVerificationRequestDto.class));

        // when
        ResultActions result =  mockMvc.perform(post("/auth/email-verifications", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(document("비밀번호_초기화를_위한_인증코드_메일_발송",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("인증코드 이메일 발송")
                                .description("이메일 주소로 인증코드를 발송합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("emailPurpose").type(JsonFieldType.STRING).description("이메일 인증코드 전송 목적(SIGNUP, PASSWORD_RESET)")
                                )
                                .build()
                        )
                ));

    }

    @Test
    void 인증번호로_이메일_인증() throws Exception {
        // given
        CodeVerificationRequestDto request = new CodeVerificationRequestDto("daramg123@gmail.com", "123456");

        doNothing().when(mailVerificationService).verifyEmailWithCode(any(CodeVerificationRequestDto.class));

        // when
        ResultActions result =  mockMvc.perform(post("/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(document("인증번호로_이메일_인증",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("인증번호로 이메일 인증")
                                .description("사용자가 제공한 인증번호로 이메일 주소를 인증합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("verificationCode").type(JsonFieldType.STRING).description("인증번호: 6자리 숫자")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 회원가입() throws Exception {
        // given
        SignupRequestDto request = new SignupRequestDto(
                "권순영",
                LocalDate.of(1996, 6, 15),
                "hamster@gmail.com",
                "Password123!",
                "https://example.com/profile.jpg",
                "햄쥑이",
                "나라 지키는 중"
        );

        doNothing().when(authService).signup(any(SignupRequestDto.class));

        // when
        ResultActions result = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andDo(document("회원가입",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원가입")
                                .description("새로운 유저를 생성합니다.")
                                .requestFields(
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("birthdate").type(JsonFieldType.STRING).description("생년월일 (YYYY-MM-DD)"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (영어 대/소문자, 숫자, 특수문자 포함 10자 이상)"),
                                        fieldWithPath("profileImage").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임 (2~8자)"),
                                        fieldWithPath("bio").type(JsonFieldType.STRING).description("bio (12자 이하)").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 비밀번호_잊었을_경우_재설정() throws Exception {
        // given
        String email = "svt@pledis.com";
        PasswordRequestDto request = new PasswordRequestDto(email, "NewPassword123!");

        doNothing().when(authService).resetPassword(any(PasswordRequestDto.class));
        doNothing().when(authService).logout(any(User.class));

        // when
        ResultActions result = mockMvc.perform(put("/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(document("비밀번호_재설정",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("비밀번호를 잊었을 경우 비밀번호 재설정")
                                .description("사용자의 비밀번호를 새로운 비밀번호로 변경하고 자동으로 로그아웃 처리합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("변경하고자 하는 유저의 이메일"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("새 비밀번호 (영어 대/소문자, 숫자, 특수문자 포함 10자 이상)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void 로그인() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto(
                "hamster@gmail.com", "Hoshi0615!");

        TokenResponseDto tokens = new TokenResponseDto(
                "access-token-123",
                "refresh-token-456"
        );

        when(authService.login(any(LoginRequestDto.class))).thenReturn(tokens);

        // when
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
                .andExpect(cookie().exists(ACCESS_COOKIE_NAME))
                .andExpect(cookie().exists(REFRESH_COOKIE_NAME))
                .andExpect(cookie().value(ACCESS_COOKIE_NAME, "access-token-123"))
                .andExpect(cookie().value(REFRESH_COOKIE_NAME, "refresh-token-456"))
                .andExpect(status().isOk())
                .andDo(document("로그인",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로그인")
                                .description("유저가 로그인합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (영어 대/소문자, 숫자, 특수문자 포함 10자 이상)")
                                )
                                .build()
                        ),
                        responseCookies(
                                cookieWithName(ACCESS_COOKIE_NAME).description("액세스 토큰 (JWT)"),
                                cookieWithName(REFRESH_COOKIE_NAME).description("리프레시 토큰 (JWT)")
                        )
                ));
    }

    @Test
    void 토큰_갱신() throws Exception {
        // given
        TokenResponseDto tokens = new TokenResponseDto(
                "new-access-token-789",
                "refresh-token-456"
        );

        when(authService.refreshAccessToken(any(String.class))).thenReturn(tokens);

        // when
        ResultActions result = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie(REFRESH_COOKIE_NAME, "refresh-token-456")));

        // then
        result
                .andExpect(cookie().exists(ACCESS_COOKIE_NAME))
                .andExpect(cookie().value(ACCESS_COOKIE_NAME, "new-access-token-789"))
                .andExpect(status().isOk())
                .andDo(document("토큰_갱신",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("토큰 갱신")
                                .description("액세시 토큰 만료 시 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
                                .build()
                        ),
                        responseCookies(
                                cookieWithName(ACCESS_COOKIE_NAME).description("새로운 액세스 토큰 (JWT)")
                        )
                ));
    }

    @Test
    void 로그아웃() throws Exception {
        // given
        doNothing().when(authService).logout(any(User.class));

        // when
        ResultActions result = mockMvc.perform(delete("/auth/logout")
                .cookie(new Cookie(COOKIE_NAME, "access_token")));

        // then
        result
                .andExpect(cookie().exists(ACCESS_COOKIE_NAME))
                .andExpect(cookie().exists(REFRESH_COOKIE_NAME))
                .andExpect(cookie().value(ACCESS_COOKIE_NAME, ""))
                .andExpect(cookie().value(REFRESH_COOKIE_NAME, ""))
                .andExpect(status().isOk())
                .andDo(document("로그아웃",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로그아웃")
                                .description("사용자를 로그아웃하고 인증 쿠키를 삭제합니다.")
                                .build()
                        ),
                        responseCookies(
                                cookieWithName(ACCESS_COOKIE_NAME).description("삭제된 액세스 토큰 쿠키"),
                                cookieWithName(REFRESH_COOKIE_NAME).description("삭제된 리프레시 토큰 쿠키")
                        )
                ));
    }
}

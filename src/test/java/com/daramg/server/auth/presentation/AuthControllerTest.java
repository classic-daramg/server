package com.daramg.server.auth.presentation;

import com.daramg.server.auth.domain.EmailPurpose;
import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.application.AuthService;
import com.daramg.server.auth.dto.EmailVerificationRequest;
import com.daramg.server.auth.dto.CodeVerificationRequest;
import com.daramg.server.auth.dto.SignupDto;
import com.daramg.server.auth.dto.PasswordDto;
import com.daramg.server.domain.user.domain.User;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MailVerificationService mailVerificationService;
    
    @MockitoBean
    private AuthService authService;

    @Test
    void 회원가입을_위한_인증코드_메일_발송() throws Exception {
        // given
        EmailVerificationRequest request = new EmailVerificationRequest("daramg123@gmail.com", EmailPurpose.SIGNUP);

        doNothing().when(mailVerificationService).sendVerificationEmail(any(EmailVerificationRequest.class));

        // when
        ResultActions result =  mockMvc.perform(post("/auth/email-verifications", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(document("회원가입을_위한_인증코드_메일_발송",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원가입 시 인증코드 이메일 발송")
                                .description("새로운 사용자를 위해 이메일 주소로 인증코드를 발송합니다.")
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
        EmailVerificationRequest request = new EmailVerificationRequest("daramg123@gmail.com", EmailPurpose.PASSWORD_RESET);

        doNothing().when(mailVerificationService).sendVerificationEmail(any(EmailVerificationRequest.class));

        // when
        ResultActions result =  mockMvc.perform(post("/auth/email-verifications", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(document("비밀번호_초기화를_위한_인증코드_메일_발송",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("비밀번호 초기화 시 인증코드 이메일 발송")
                                .description("비밀번호 초기화를 위해 이메일 주소로 인증코드를 발송합니다.")
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
        CodeVerificationRequest request = new CodeVerificationRequest("daramg123@gmail.com", "123456");

        doNothing().when(mailVerificationService).verifyEmailWithCode(any(CodeVerificationRequest.class));

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
        SignupDto request = new SignupDto(
                "권순영",
                LocalDate.of(1996, 6, 15),
                "hamster@gmail.com",
                "Password123!",
                "https://example.com/profile.jpg",
                "햄쥑이",
                "나라 지키는 중"
        );

        doNothing().when(authService).signup(any(SignupDto.class));

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
    void 비밀번호_재설정() throws Exception {
        // given
        PasswordDto request = new PasswordDto("NewPassword123!");
        
        // User 객체를 모킹하기 위해 임시로 사용
        User mockUser = Mockito.mock(User.class);

        doNothing().when(authService).resetPassword(any(PasswordDto.class), any(User.class));

        // when
        ResultActions result = mockMvc.perform(put("/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(document("비밀번호_재설정",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("비밀번호 재설정")
                                .description("사용자의 비밀번호를 새로운 비밀번호로 변경합니다.")
                                .requestFields(
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("새 비밀번호 (영어 대/소문자, 숫자, 특수문자 포함 10자 이상)")
                                )
                                .build()
                        )
                ));
    }
}

package com.daramg.server.auth.controller;

import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.dto.EmailRequest;
import com.daramg.server.auth.dto.VerificationMailRequest;
import com.daramg.server.auth.presentation.AuthController;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MailVerificationService mailVerificationService;

    @Test
    void 회원가입을_위한_인증코드_메일_발송() throws Exception {
        // given
        EmailRequest request = new EmailRequest("daramg123@gmail.com");

        doNothing().when(mailVerificationService).sendVerificationEmail(Mockito.any(EmailRequest.class));

        // when
        ResultActions result =  mockMvc.perform(post("/send-verification-email", request)
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
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일")
                                )
                                .build()
                        )
                ));

    }

    @Test
    void 인증번호로_이메일_인증() throws Exception {
        // given
        VerificationMailRequest request = new VerificationMailRequest("daramg123@gmail.com", "123456");

        doNothing().when(mailVerificationService).verifyEmailWithCode(Mockito.any(VerificationMailRequest.class));

        // when
        ResultActions result =  mockMvc.perform(post("/verify-email")
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
}

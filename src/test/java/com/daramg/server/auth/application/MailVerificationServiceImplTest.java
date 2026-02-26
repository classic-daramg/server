package com.daramg.server.auth.application;

import com.daramg.server.auth.domain.EmailPurpose;
import com.daramg.server.auth.dto.EmailVerificationRequestDto;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.auth.repository.RateLimitRepository;
import com.daramg.server.auth.repository.VerificationCodeRepository;
import com.daramg.server.auth.util.MailContentBuilder;
import com.daramg.server.auth.util.MimeMessageGenerator;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailVerificationServiceImplTest {

    @Mock private MimeMessageGenerator mimeMessageGenerator;
    @Mock private MailContentBuilder mailContentBuilder;
    @Mock private JavaMailSender javaMailSender;
    @Mock private VerificationCodeRepository verificationCodeRepository;
    @Mock private RateLimitRepository rateLimitRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private MailVerificationServiceImpl mailVerificationService;

    private static final String TEST_EMAIL = "test@daramg.com";

    @Nested
    @DisplayName("인증코드 발송 시")
    class SendVerificationEmail {

        @Test
        @DisplayName("새 코드 발급 시 시도 횟수를 초기화하지 않는다")
        void 새_코드_발급_시_시도_횟수_초기화_안됨() throws Exception {
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(rateLimitRepository.isRateLimited(TEST_EMAIL)).willReturn(false);
            given(mailContentBuilder.buildVerificationEmail(anyString())).willReturn("<html>code</html>");
            given(mimeMessageGenerator.generate(anyString(), anyString(), anyString()))
                    .willReturn(mock(MimeMessage.class));

            EmailVerificationRequestDto request = new EmailVerificationRequestDto(null, TEST_EMAIL, EmailPurpose.SIGNUP);
            mailVerificationService.sendVerificationEmail(request);

            verify(rateLimitRepository, never()).resetAttempts(TEST_EMAIL);
        }

        @Test
        @DisplayName("레이트 리밋 초과 시 예외가 발생한다")
        void 레이트_리밋_초과_시_예외_발생() {
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(rateLimitRepository.isRateLimited(TEST_EMAIL)).willReturn(true);

            EmailVerificationRequestDto request = new EmailVerificationRequestDto(null, TEST_EMAIL, EmailPurpose.SIGNUP);

            assertThatThrownBy(() -> mailVerificationService.sendVerificationEmail(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorStatus.EMAIL_RATE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("이미 가입된 이메일로 SIGNUP 요청 시 예외가 발생한다")
        void 중복_이메일_SIGNUP_예외_발생() {
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

            EmailVerificationRequestDto request = new EmailVerificationRequestDto(null, TEST_EMAIL, EmailPurpose.SIGNUP);

            assertThatThrownBy(() -> mailVerificationService.sendVerificationEmail(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorStatus.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("미가입 이메일로 PASSWORD_RESET 요청 시 예외가 발생한다")
        void 미가입_이메일_PASSWORD_RESET_예외_발생() {
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);

            EmailVerificationRequestDto request = new EmailVerificationRequestDto(null, TEST_EMAIL, EmailPurpose.PASSWORD_RESET);

            assertThatThrownBy(() -> mailVerificationService.sendVerificationEmail(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorStatus.EMAIL_NOT_REGISTERED);
        }
    }

    @Nested
    @DisplayName("인증코드 검증 시")
    class VerifyEmailWithCode {

        @Test
        @DisplayName("인증 성공 시 시도 횟수를 초기화한다")
        void 인증_성공_시_시도_횟수_초기화() {
            given(rateLimitRepository.isAttemptExceeded(TEST_EMAIL)).willReturn(false);
            given(verificationCodeRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of("123456"));
            doNothing().when(verificationCodeRepository).deleteByEmail(TEST_EMAIL);
            doNothing().when(rateLimitRepository).resetAttempts(TEST_EMAIL);

            com.daramg.server.auth.dto.CodeVerificationRequestDto request =
                    new com.daramg.server.auth.dto.CodeVerificationRequestDto(TEST_EMAIL, "123456");
            mailVerificationService.verifyEmailWithCode(request);

            verify(rateLimitRepository).resetAttempts(TEST_EMAIL);
        }

        @Test
        @DisplayName("검증 시도 횟수 초과 시 예외가 발생한다")
        void 시도_횟수_초과_시_예외_발생() {
            given(rateLimitRepository.isAttemptExceeded(TEST_EMAIL)).willReturn(true);

            com.daramg.server.auth.dto.CodeVerificationRequestDto request =
                    new com.daramg.server.auth.dto.CodeVerificationRequestDto(TEST_EMAIL, "123456");

            assertThatThrownBy(() -> mailVerificationService.verifyEmailWithCode(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorStatus.VERIFICATION_ATTEMPT_EXCEEDED);
        }

        @Test
        @DisplayName("잘못된 인증코드 입력 시 시도 횟수가 증가한다")
        void 틀린_코드_시도_횟수_증가() {
            given(rateLimitRepository.isAttemptExceeded(TEST_EMAIL)).willReturn(false);
            given(verificationCodeRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of("123456"));

            com.daramg.server.auth.dto.CodeVerificationRequestDto request =
                    new com.daramg.server.auth.dto.CodeVerificationRequestDto(TEST_EMAIL, "999999");

            assertThatThrownBy(() -> mailVerificationService.verifyEmailWithCode(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorStatus.CODE_VERIFICATION_FAILED);

            verify(rateLimitRepository).incrementAttempt(TEST_EMAIL);
        }
    }
}

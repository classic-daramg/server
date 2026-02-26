package com.daramg.server.auth.application;

import com.daramg.server.auth.domain.MailMessages;
import com.daramg.server.auth.dto.EmailVerificationRequestDto;
import com.daramg.server.auth.dto.CodeVerificationRequestDto;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.auth.repository.VerificationCodeRepository;
import com.daramg.server.auth.util.MailContentBuilder;
import com.daramg.server.auth.util.MimeMessageGenerator;
import com.daramg.server.auth.util.VerificationCodeGenerator;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.exception.UserErrorStatus;
import com.daramg.server.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.daramg.server.auth.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailVerificationServiceImpl implements MailVerificationService{

    private final MimeMessageGenerator mimeMessageGenerator;
    private final MailContentBuilder mailContentBuilder;
    private final JavaMailSender javaMailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final RateLimitRepository rateLimitRepository;
    private final UserRepository userRepository;

    public void sendVerificationEmail(EmailVerificationRequestDto request) {
        switch (request.getEmailPurpose()) {
            case SIGNUP -> sendForSignup(request);
            case PASSWORD_RESET -> sendForPasswordReset(request);
            case EMAIL_CHANGE -> sendForEmailChange(request);
            default -> throw new BusinessException(AuthErrorStatus.UNSUPPORTED_EMAIL_PURPOSE);
        }
    }

    private void sendForSignup(EmailVerificationRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(AuthErrorStatus.DUPLICATE_EMAIL);
        }
        sendVerificationCode(request);
    }

    private void sendForPasswordReset(EmailVerificationRequestDto request) {
        if (!userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException(AuthErrorStatus.EMAIL_NOT_REGISTERED);
        }
        sendVerificationCode(request);
    }

    private void sendForEmailChange(EmailVerificationRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())
                && !request.getEmail().equals(request.getOriginalEmail())) {
            throw new BusinessException(UserErrorStatus.DUPLICATE_EMAIL);
        }
        sendVerificationCode(request);
    }

    private void sendVerificationCode(EmailVerificationRequestDto request) {
        executeRedisOperationVoid(() -> {
            if (rateLimitRepository.isRateLimited(request.getEmail())) {
                throw new BusinessException(AuthErrorStatus.EMAIL_RATE_LIMIT_EXCEEDED);
            }
        });

        String verificationCode = VerificationCodeGenerator.generate();

        executeRedisOperationVoid(() ->
            verificationCodeRepository.save(request.getEmail(), verificationCode)
        );

        try {
            String htmlContent = mailContentBuilder.buildVerificationEmail(verificationCode);
            MimeMessage mimeMessage = mimeMessageGenerator.generate(
                    request.getEmail(),
                    MailMessages.MAIL_VERIFICATION_SUBJECT,
                    htmlContent
            );

            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("이메일 발송 실패 - email: {}, error: {}", request.getEmail(), e.getMessage());
            throw new BusinessException(AuthErrorStatus.SEND_VERIFICATION_EMAIL_FAILED);
        }
    }

    public void verifyEmailWithCode(CodeVerificationRequestDto request) {
        executeRedisOperationVoid(() -> {
            if (rateLimitRepository.isAttemptExceeded(request.getEmail())) {
                throw new BusinessException(AuthErrorStatus.VERIFICATION_ATTEMPT_EXCEEDED);
            }
        });

        String storedCode = executeRedisOperation(() ->
            verificationCodeRepository.findByEmail(request.getEmail()).orElse(null)
        );

        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            executeRedisOperationVoid(() -> rateLimitRepository.incrementAttempt(request.getEmail()));
            throw new BusinessException(AuthErrorStatus.CODE_VERIFICATION_FAILED);
        }

        executeRedisOperationVoid(() -> {
            verificationCodeRepository.deleteByEmail(request.getEmail());
            rateLimitRepository.resetAttempts(request.getEmail());
        });
    }

    private <T> T executeRedisOperation(java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패: ", e);
            throw new BusinessException(AuthErrorStatus.REDIS_CONNECTION_FAILED);
        } catch (Exception e) {
            // Redis 관련 기타 예외 처리 (RedisSystemException, ConnectException 등)
            if (e.getCause() instanceof java.net.ConnectException ||
                e.getMessage() != null && e.getMessage().contains("Redis") ||
                e.getClass().getName().contains("Redis")) {
                log.error("Redis 연결 오류: ", e);
                throw new BusinessException(AuthErrorStatus.REDIS_CONNECTION_FAILED);
            }
            throw e;
        }
    }

    private void executeRedisOperationVoid(Runnable operation) {
        executeRedisOperation(() -> {
            operation.run();
            return null;
        });
    }
}

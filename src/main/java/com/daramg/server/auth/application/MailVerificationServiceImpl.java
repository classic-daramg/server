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
import com.daramg.server.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailVerificationServiceImpl implements MailVerificationService{

    private final MimeMessageGenerator mimeMessageGenerator;
    private final MailContentBuilder mailContentBuilder;
    private final JavaMailSender javaMailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;

    public void sendVerificationEmail(EmailVerificationRequestDto request) {
        switch (request.getEmailPurpose()) {
            case SIGNUP -> sendForSignup(request);
            case PASSWORD_RESET -> sendForPasswordReset(request);
            default -> throw new BusinessException("지원하지 않는 이메일 발송 목적입니다.");
        }
    }

    private void sendForSignup(EmailVerificationRequestDto request) {
        sendVerificationCode(request);
    }

    private void sendForPasswordReset(EmailVerificationRequestDto request) {
        if (!userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException(AuthErrorStatus.EMAIL_NOT_REGISTERED);
        }
        sendVerificationCode(request);
    }

    private void sendVerificationCode(EmailVerificationRequestDto request) {
        String verificationCode = VerificationCodeGenerator.generate();
        verificationCodeRepository.save(request.getEmail(), verificationCode);
        
        try {
            String htmlContent = mailContentBuilder.buildVerificationEmail(verificationCode);
            MimeMessage mimeMessage = mimeMessageGenerator.generate(
                    request.getEmail(),
                    MailMessages.MAIL_VERIFICATION_SUBJECT,
                    htmlContent
            );

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException(AuthErrorStatus.SEND_VERIFICATION_EMAIL_FAILED);
        }
    }

    public void verifyEmailWithCode(CodeVerificationRequestDto request) {
        String storedCode = verificationCodeRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            throw new BusinessException(AuthErrorStatus.CODE_VERIFICATION_FAILED);
        }
        verificationCodeRepository.deleteByEmail(request.getEmail());
    }
}

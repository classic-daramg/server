package com.daramg.server.auth.application;

import com.daramg.server.auth.domain.MailMessages;
import com.daramg.server.auth.util.MailContentBuilder;
import com.daramg.server.auth.util.MimeMessageGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncMailSender {

    private final JavaMailSender javaMailSender;
    private final MimeMessageGenerator mimeMessageGenerator;
    private final MailContentBuilder mailContentBuilder;

    @Async("mailTaskExecutor")
    public void sendVerificationCode(String email, String verificationCode) {
        try {
            String htmlContent = mailContentBuilder.buildVerificationEmail(verificationCode);
            MimeMessage mimeMessage = mimeMessageGenerator.generate(
                    email,
                    MailMessages.MAIL_VERIFICATION_SUBJECT,
                    htmlContent
            );
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("이메일 발송 실패 - email: {}, error: {}", email, e.getMessage());
        }
    }
}

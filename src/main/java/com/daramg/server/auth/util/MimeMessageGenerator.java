package com.daramg.server.auth.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MimeMessageGenerator {

    private final JavaMailSender javaMailSender;

    public MimeMessage generate(String email, String mailSubject, String htmlContent) throws MessagingException {
        MimeMessage mm = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mm, "utf-8");

        helper.setTo(email);
        helper.setSubject(mailSubject);
        helper.setText(htmlContent, true);

        return mm;
    }
}

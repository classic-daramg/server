package com.daramg.server.auth.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
@RequiredArgsConstructor
public class MimeMessageGenerator {

    private final JavaMailSender javaMailSender;

    @Value("${mail.sender-email}")
    private String senderEmail;

    @Value("${mail.sender-name}")
    private String senderName;

    public MimeMessage generate(String email, String mailSubject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mm = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mm, "utf-8");

        helper.setFrom(new InternetAddress(senderEmail, senderName));
        helper.setTo(email);
        helper.setSubject(mailSubject);
        helper.setText(htmlContent, true);

        return mm;
    }
}

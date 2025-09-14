package com.daramg.server.auth.presentation;

import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.dto.EmailRequest;
import com.daramg.server.auth.dto.VerificationMailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final MailVerificationService mailVerificationService;

    @PostMapping("/send-verification-email")
    @ResponseStatus(HttpStatus.OK)
    public void sendVerificationEmail(@RequestBody @Valid EmailRequest request) {
        mailVerificationService.sendVerificationEmail(request);
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public void verify(@RequestBody @Valid VerificationMailRequest request) {
        mailVerificationService.verifyEmailWithCode(request);
    }
}

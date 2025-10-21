package com.daramg.server.auth.presentation;

import com.daramg.server.auth.application.AuthService;
import com.daramg.server.auth.application.MailVerificationService;
import com.daramg.server.auth.dto.EmailVerificationRequest;
import com.daramg.server.auth.dto.CodeVerificationRequest;
import com.daramg.server.domain.user.domain.User;
import com.daramg.server.auth.dto.LoginDto;
import com.daramg.server.auth.dto.PasswordDto;
import com.daramg.server.auth.dto.SignupDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MailVerificationService mailVerificationService;
    private final AuthService authService;

    @PostMapping("/email-verifications")
    @ResponseStatus(HttpStatus.OK)
    public void sendVerificationEmail(@RequestBody @Valid EmailVerificationRequest request) {
        mailVerificationService.sendVerificationEmail(request);
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public void verify(@RequestBody @Valid CodeVerificationRequest request) {
        mailVerificationService.verifyEmailWithCode(request);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupDto request) {
        authService.signup(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestBody LoginDto request) {
        authService.login(request);
    }

    @PutMapping("/password-reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody PasswordDto request, User user){
        authService.resetPassword(request, user);
    }
}

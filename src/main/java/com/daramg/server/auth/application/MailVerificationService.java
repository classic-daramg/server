package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.EmailVerificationRequestDto;
import com.daramg.server.auth.dto.CodeVerificationRequestDto;

public interface MailVerificationService {
    void sendVerificationEmail(EmailVerificationRequestDto request);
    void verifyEmailWithCode(CodeVerificationRequestDto request);
}

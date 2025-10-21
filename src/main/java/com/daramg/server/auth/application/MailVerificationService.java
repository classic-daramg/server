package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.EmailVerificationRequest;
import com.daramg.server.auth.dto.CodeVerificationRequest;

public interface MailVerificationService {
    void sendVerificationEmail(EmailVerificationRequest request);
    void verifyEmailWithCode(CodeVerificationRequest request);
}

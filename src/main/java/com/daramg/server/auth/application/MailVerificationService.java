package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.EmailRequest;
import com.daramg.server.auth.dto.VerificationMailRequest;

public interface MailVerificationService {
    void sendVerificationEmail(EmailRequest request);
    void verifyEmailWithCode(VerificationMailRequest request);
}

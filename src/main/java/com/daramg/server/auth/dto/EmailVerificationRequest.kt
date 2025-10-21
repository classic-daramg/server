package com.daramg.server.auth.dto

import com.daramg.server.auth.domain.EmailPurpose
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EmailVerificationRequest(
    @field:NotBlank(message = "AUTH_400_2")
    @field:Email(message = "AUTH_400_3")
    val email: String,

    @field:NotNull(message = "인증코드 전송 목적은 필수값입니다")
    val emailPurpose: EmailPurpose
)

package com.daramg.server.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CodeVerificationRequest(
    @field:NotBlank(message = "AUTH_400_2")
    @field:Email(message = "AUTH_400_3")
    val email: String,

    @field:NotBlank(message = "AUTH_400_4")
    @field:Pattern(
        regexp = VERIFICATION_CODE_REGEX,
        message = "AUTH_400_5")
    val verificationCode: String
) {
    companion object {
        private const val VERIFICATION_CODE_REGEX = "^\\d{6}$"
    }
}

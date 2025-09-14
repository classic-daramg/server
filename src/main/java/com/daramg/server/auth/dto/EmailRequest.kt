package com.daramg.server.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailRequest(
    @field:NotBlank(message = "AUTH_400_2")
    @field:Email(message = "AUTH_400_3")
    val email: String
)

package com.daramg.server.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class EmailChangeRequestDto(
    @field:NotNull
    @field:Email(message = "AUTH_400_3")
    val email: String,
)
package com.daramg.server.user.dto

import jakarta.validation.constraints.Pattern

data class PasswordRequestDto(
    @get:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()]).{10,}\$",
        message = "비밀번호는 영어 대문자와 소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다"
    )
    val password: String
)
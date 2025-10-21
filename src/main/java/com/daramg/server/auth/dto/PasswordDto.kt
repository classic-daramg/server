package com.daramg.server.auth.dto

import jakarta.validation.constraints.Pattern

class PasswordDto(
    @get:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()]).{10,}\$",
        message = "비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다"
    )
    val password: String
)

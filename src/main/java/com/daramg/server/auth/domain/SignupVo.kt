package com.daramg.server.auth.domain

import java.time.LocalDate

class SignupVo (
    val name: String,
    val birthDate: LocalDate,
    val email: String,
    val password: String,
    val profileImage: String?,
    val nickname: String,
    val bio: String?
)

package com.daramg.server.auth.dto

data class TokenResponseDto(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String
)
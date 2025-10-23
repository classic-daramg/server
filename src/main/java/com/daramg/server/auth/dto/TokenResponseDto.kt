package com.daramg.server.auth.dto

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)
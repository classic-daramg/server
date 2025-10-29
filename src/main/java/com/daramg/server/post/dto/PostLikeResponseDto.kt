package com.daramg.server.post.dto

data class PostLikeResponseDto(
    val isLiked: Boolean,
    val likeCount: Int
)
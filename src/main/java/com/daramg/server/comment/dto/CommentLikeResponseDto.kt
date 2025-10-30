package com.daramg.server.comment.dto

data class CommentLikeResponseDto(
    val isLiked: Boolean,
    val likeCount: Int
)
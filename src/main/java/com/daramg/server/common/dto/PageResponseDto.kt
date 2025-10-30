package com.daramg.server.common.dto

data class PageResponseDto<T>(
    val content: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean
)

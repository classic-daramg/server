package com.daramg.server.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentReplyCreateDto(
    @field:NotBlank(message = "대댓글 내용은 비어있을 수 없습니다.")
    @field:Size(max = 500, message = "대댓글은 500자를 초과할 수 없습니다.")
    val content: String
)
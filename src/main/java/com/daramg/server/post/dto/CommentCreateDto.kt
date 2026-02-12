package com.daramg.server.post.dto

import com.daramg.server.common.validation.NoBadWords
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentCreateDto(
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    @field:Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다.")
    @field:NoBadWords
    val content: String
)
package com.daramg.server.notice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NoticeCreateDto (

    @get:NotBlank(message = "제목 입력은 필수입니다")
    @get:Size(max = 15, message = "15자 이내로 입력해주세요")
    val title: String,

    @get:NotBlank(message = "내용 입력은 필수입니다")
    @get:Size(min = 5, max = 3000, message ="5자 이상 3000자 이내로 입력해주세요")
    val content: String,

    val images: List<String> = emptyList(),

    val videoUrl: String? = null,
)

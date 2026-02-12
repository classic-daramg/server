package com.daramg.server.notice.dto

import com.daramg.server.common.validation.NoBadWords
import jakarta.validation.constraints.Size

data class NoticeUpdateDto (

    @get:Size(max = 15, message = "제목은 15자를 초과할 수 없습니다")
    @get:NoBadWords
    val title: String? = null,

    @get:Size(min = 5, max = 3000, message = "내용은 5자 이상 3000자 이하로 입력해주세요")
    @get:NoBadWords
    val content: String? = null,

    val images: List<String>? = null,
)

package com.daramg.server.user.dto

import com.daramg.server.common.validation.NoBadWords
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserProfileUpdateRequestDto(
    val profileImageUrl: String?,

    @get:Pattern(
        regexp = "^[a-zA-Z0-9가-힣._]{2,8}$",
        message = "닉네임은 2~8자의 한글, 영문, 숫자와 일부 특수문자(_, .)만 사용할 수 있습니다."
    )
    @get:NoBadWords
    val nickname: String,

    @get:Size(max = 12, message = "bio는 12자 이하로 입력해주세요")
    @get:NoBadWords
    val bio: String?
) {
}
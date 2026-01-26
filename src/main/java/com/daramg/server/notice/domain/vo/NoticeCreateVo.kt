package com.daramg.server.notice.domain.vo

import com.daramg.server.user.domain.User

class NoticeCreateVo (

    val user: User,
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
) {
}
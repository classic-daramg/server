package com.daramg.server.notice.dto


class NoticeUpdateDto (

    val title: String? = null,
    val content: String? = null,
    val images: List<String>? = null,
    val videoUrl: String? = null,
) {}
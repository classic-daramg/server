package com.daramg.server.post.domain.vo

import com.daramg.server.post.domain.PostStatus

data class PostUpdateVo (
    val title: String?,
    val content: String?,
    val postStatus: PostStatus?,
    val images: List<String>?,
    val videoUrl: String?,
    val hashtags: List<String>?
)

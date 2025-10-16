package com.daramg.server.domain.post.domain.vo

import com.daramg.server.domain.composer.domain.Composer
import com.daramg.server.domain.post.domain.PostStatus
import com.daramg.server.domain.user.domain.User

sealed class PostCreateVo (
    open val user: User,
    open val title: String,
    open val content: String,
    open val postStatus: PostStatus,
    open val images: List<String> = emptyList(),
    open val videoUrl: String? = null,
    open val hashtags: List<String> = emptyList(),
) {
    data class Free(
        override val user: User,
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList(),
    ): PostCreateVo(user, title, content, postStatus, images, videoUrl, hashtags)

    data class Story(
        override val user: User,
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList(),
        val primaryComposer: Composer? = null,
    ): PostCreateVo(user, title, content, postStatus, images, videoUrl, hashtags)

    data class Curation(
        override val user: User,
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList(),
        val primaryComposer: Composer? = null,
        val additionalComposers: List<Composer> = emptyList()
    ):  PostCreateVo(user, title, content, postStatus, images, videoUrl, hashtags)
}

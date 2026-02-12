package com.daramg.server.post.dto

import com.daramg.server.common.validation.NoBadWords
import com.daramg.server.post.domain.PostStatus
import jakarta.validation.constraints.Size

sealed class PostUpdateDto (
    @get:Size(max=15, message = "제목은 15자를 초과할 수 없습니다")
    @get:NoBadWords
    open val title: String?,

    @get:Size(min = 5, max = 3000, message = "내용은 5자 이상 3000자 이하로 입력해주세요")
    @get:NoBadWords
    open val content: String?,

    open val postStatus: PostStatus?,
    open val images: List<String>?,
    open val videoUrl: String?,
    open val hashtags: List<String>?,
){
    data class UpdateFree(
        override val title: String?,
        override val content: String?,
        override val postStatus: PostStatus?,
        override val images: List<String>?,
        override val videoUrl: String?,
        override val hashtags: List<String>?
    ) : PostUpdateDto(title, content, postStatus, images, videoUrl, hashtags)

    data class UpdateStory(
        override val title: String?,
        override val content: String?,
        override val postStatus: PostStatus?,
        override val images: List<String>?,
        override val videoUrl: String?,
        override val hashtags: List<String>?
    ) : PostUpdateDto(title, content,postStatus, images, videoUrl, hashtags)

    data class UpdateCuration(
        override val title: String?,
        override val content: String?,
        override val postStatus: PostStatus?,
        override val images: List<String>?,
        override val videoUrl: String?,
        override val hashtags: List<String>?,
        val additionalComposersId: List<Long>?
    ) : PostUpdateDto(title, content, postStatus, images, videoUrl, hashtags)
}

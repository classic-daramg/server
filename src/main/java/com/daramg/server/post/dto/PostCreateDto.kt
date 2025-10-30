package com.daramg.server.post.dto

import com.daramg.server.post.domain.PostStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

sealed class PostCreateDto(
    @get:NotBlank(message = "제목은 필수입니다")
    @get:Size(max = 15, message = "제목은 15자를 초과할 수 없습니다")
    open val title: String,

    @get:Size(min = 5, max = 3000, message = "내용은 5자 이상 3000자 이하로 입력해주세요")
    open val content: String,

    @get:NotNull(message = "게시글 상태는 필수입니다")
    open val postStatus: PostStatus,

    open val images: List<String> = emptyList(),
    open val videoUrl: String? = null,
    open val hashtags: List<String> = emptyList(),

    ) {

    data class CreateFree(
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList()
    ) : PostCreateDto(title, content, postStatus, images, videoUrl, hashtags)

    data class CreateStory(
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList(),

        @get:NotNull(message = "주요 작곡가는 필수입니다")
        val primaryComposerId: Long

    ) : PostCreateDto(title, content, postStatus, images, videoUrl, hashtags)

    data class CreateCuration(
        override val title: String,
        override val content: String,
        override val postStatus: PostStatus,
        override val images: List<String> = emptyList(),
        override val videoUrl: String? = null,
        override val hashtags: List<String> = emptyList(),

        @get:NotNull(message = "주요 작곡가는 필수입니다")
        val primaryComposerId: Long,
        val additionalComposerIds: List<Long> = emptyList()
    ) : PostCreateDto(title, content, postStatus, images, videoUrl, hashtags)
}

package com.daramg.server.domain.post.dto;

import com.daramg.server.domain.composer.Composer;
import com.daramg.server.domain.post.domain.PostStatus;
import com.daramg.server.domain.post.domain.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PostRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 15, message = "제목은 15자를 초과할 수 없습니다")
    private final String title;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 3000, message = "내용은 3000자를 초과할 수 없습니다")
    private final String content;

    @NotNull(message = "게시글 상태는 필수입니다")
    private final PostStatus postStatus;

    private final List<String> images;
    private final String videoUrl;
    private final List<String> hashtags;

    public abstract PostType getPostType();

    @Getter
    @SuperBuilder(toBuilder = true)
    public static class CreateFree extends PostRequest {
        @Override
        public PostType getPostType() {
            return PostType.FREE;
        }
    }

    @Getter
    @SuperBuilder(toBuilder = true)
    public static class CreateStory extends PostRequest {
        @NotNull(message = "주요 작곡가는 필수입니다")
        private final Composer primaryComposer;

        @Override
        public PostType getPostType() {
            return PostType.STORY;
        }
    }

    @Getter
    @SuperBuilder(toBuilder = true)
    public static class CreateCuration extends PostRequest {
        @NotNull(message = "주요 작곡가는 필수입니다")
        private final Composer primaryComposer;

        private final List<Composer> additionalComposers;

        @Override
        public PostType getPostType() {
            return PostType.CURATION;
        }
    }
}

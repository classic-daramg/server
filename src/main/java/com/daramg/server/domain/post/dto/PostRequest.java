package com.daramg.server.domain.post.dto;

import com.daramg.server.domain.composer.Composer;
import com.daramg.server.domain.post.domain.PostStatus;
import com.daramg.server.domain.post.domain.PostType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PostRequest {

    private final String title;
    private final String content;
    private final List<String> images;
    private final String videoUrl;
    private final PostStatus postStatus;
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
        private final Composer primaryComposer;

        @Override
        public PostType getPostType() {
            return PostType.STORY;
        }
    }

    @Getter
    @SuperBuilder(toBuilder = true)
    public static class CreateCuration extends PostRequest {
        private final Composer primaryComposer;
        private final List<Composer> additionalComposers;

        @Override
        public PostType getPostType() {
            return PostType.CURATION;
        }
    }
}

package com.daramg.server.search.dto;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostType;

import java.time.Instant;
import java.util.List;

public record SearchResponseDto(
        List<ComposerResult> composers,
        List<PostResult> posts
) {
    public record ComposerResult(
            Long id,
            String koreanName,
            String englishName
    ) {
        public static ComposerResult from(Composer composer) {
            return new ComposerResult(
                    composer.getId(),
                    composer.getKoreanName(),
                    composer.getEnglishName()
            );
        }
    }

    public record PostResult(
            Long id,
            String title,
            PostType type,
            String writerNickname,
            Instant createdAt
    ) {
        public static PostResult from(Post post, PostType type) {
            return new PostResult(
                    post.getId(),
                    post.getTitle(),
                    type,
                    post.getUser().getNickname(),
                    post.getCreatedAt()
            );
        }
    }
}

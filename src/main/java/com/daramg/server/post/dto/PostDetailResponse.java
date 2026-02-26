package com.daramg.server.post.dto;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.comment.dto.CommentResponseDto;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.PostType;
import com.daramg.server.post.domain.StoryPost;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record PostDetailResponse(
        Long id,
        String writerNickname,
        String writerProfileImage,
        String title,
        String content,
        List<String> images,
        String videoUrl,
        List<String> hashtags,
        PostStatus postStatus,
        int likeCount,
        int commentCount,
        int viewCount,
        boolean isBlocked,
        Instant createdAt,
        Instant updatedAt,
        PostType type,
        ComposerInfo primaryComposer,
        List<ComposerInfo> additionalComposers,
        Boolean isLiked,
        Boolean isScrapped,
        List<CommentResponseDto> comments
) {
    public static PostDetailResponse from(Post post) {
        return from(post, null, null, List.of());
    }

    public static PostDetailResponse from(Post post, Boolean isLiked, Boolean isScrapped) {
        return from(post, isLiked, isScrapped, List.of());
    }

    public static PostDetailResponse from(Post post, Boolean isLiked, Boolean isScrapped, List<CommentResponseDto> comments) {
        PostType type = getPostType(post);
        ComposerInfo primaryComposer = null;
        List<ComposerInfo> additionalComposers = null;

        if (post instanceof StoryPost storyPost) {
            if (storyPost.getPrimaryComposer() != null) {
                primaryComposer = ComposerInfo.from(storyPost.getPrimaryComposer());
            }
        } else if (post instanceof CurationPost curationPost) {
            if (curationPost.getPrimaryComposer() != null) {
                primaryComposer = ComposerInfo.from(curationPost.getPrimaryComposer());
            }
            if (curationPost.getAdditionalComposers() != null && !curationPost.getAdditionalComposers().isEmpty()) {
                additionalComposers = curationPost.getAdditionalComposers().stream()
                        .map(ComposerInfo::from)
                        .collect(Collectors.toList());
            }
        }

        return new PostDetailResponse(
                post.getId(),
                post.getUser().getNickname(),
                post.getUser().getProfileImage(),
                post.getTitle(),
                post.getContent(),
                post.getImages(),
                post.getVideoUrl(),
                post.getHashtags(),
                post.getPostStatus(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.isBlocked(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                type,
                primaryComposer,
                additionalComposers,
                isLiked,
                isScrapped,
                comments
        );
    }

    private static PostType getPostType(Post post) {
        if (post instanceof StoryPost) {
            return PostType.STORY;
        } else if (post instanceof FreePost) {
            return PostType.FREE;
        } else if (post instanceof CurationPost) {
            return PostType.CURATION;
        }
        throw new BusinessException("유효하지 않은 Post 타입입니다: " + post.getClass().getName());
    }

    public record ComposerInfo(
            Long id,
            String koreanName,
            String englishName
    ) {
        public static ComposerInfo from(Composer composer) {
            return new ComposerInfo(
                    composer.getId(),
                    composer.getKoreanName(),
                    composer.getEnglishName()
            );
        }
    }
}


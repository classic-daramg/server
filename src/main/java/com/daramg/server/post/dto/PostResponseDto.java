package com.daramg.server.post.dto;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostType;
import com.daramg.server.post.domain.StoryPost;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
        Long id,
        String title,
        String content,
        List<String> hashtags,
        LocalDateTime createdAt,
        String writerNickname,
        int likeCount,
        int commentCount,
        String thumbnailImageUrl,
        PostType type,
        Boolean isLiked,
        Boolean isScrapped
) {
    public static PostResponseDto from(Post post) {
        return from(post, null, null);
    }

    public static PostResponseDto from(Post post, Boolean isLiked, Boolean isScrapped) {
        List<String> imageUrls = post.getImages();
        PostType type = getPostType(post);
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getHashtags(),
                post.getCreatedAt(),
                post.getUser().getNickname(),
                post.getLikeCount(),
                post.getCommentCount(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst(),
                type,
                isLiked,
                isScrapped
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
}

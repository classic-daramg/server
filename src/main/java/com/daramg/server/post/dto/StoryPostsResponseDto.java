package com.daramg.server.post.dto;

import com.daramg.server.post.domain.StoryPost;

import java.time.LocalDateTime;
import java.util.List;

public record StoryPostsResponseDto(
        String title,
        String content,
        List<String> hashtags,
        LocalDateTime createdAt,
        String writerNickname,
        int likeCount,
        int commentCount,
        String thumbnailImageUrl
) {
    public static StoryPostsResponseDto from(StoryPost storyPost) {
        List<String> imageUrls = storyPost.getImages();
        return new StoryPostsResponseDto(
                storyPost.getTitle(),
                storyPost.getContent(),
                storyPost.getHashtags(),
                storyPost.getCreatedAt(),
                storyPost.getUser().getNickname(),
                storyPost.getLikeCount(),
                storyPost.getCommentCount(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst()
        );
    }
}


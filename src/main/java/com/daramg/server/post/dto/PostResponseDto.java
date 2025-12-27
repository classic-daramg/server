package com.daramg.server.post.dto;

import com.daramg.server.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
        String title,
        String content,
        List<String> hashtags,
        LocalDateTime createdAt,
        String writerNickname,
        int likeCount,
        int commentCount,
        String thumbnailImageUrl
) {
    public static PostResponseDto from(Post post) {
        List<String> imageUrls = post.getImages();
        return new PostResponseDto(
                post.getTitle(),
                post.getContent(),
                post.getHashtags(),
                post.getCreatedAt(),
                post.getUser().getNickname(),
                post.getLikeCount(),
                post.getCommentCount(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst()
        );
    }
}


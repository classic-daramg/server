package com.daramg.server.post.dto;

import com.daramg.server.post.domain.FreePost;

import java.time.LocalDateTime;
import java.util.List;

public record FreePostsResponseDto(
        String title,
        String content,
        List<String> hashtags,
        LocalDateTime createdAt,
        String writerNickname,
        int likeCount,
        int commentCount,
        String thumbnailImageUrl
) {
    public static FreePostsResponseDto from(FreePost freePost) {
        List<String> imageUrls = freePost.getImages();
        return new FreePostsResponseDto(
                freePost.getTitle(),
                freePost.getContent(),
                freePost.getHashtags(),
                freePost.getCreatedAt(),
                freePost.getUser().getNickname(),
                freePost.getLikeCount(),
                freePost.getCommentCount(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst()
        );
    }
}

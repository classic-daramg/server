package com.daramg.server.post.dto;

import com.daramg.server.post.domain.CurationPost;

import java.time.LocalDateTime;
import java.util.List;

public record CurationPostsResponseDto(
        String title,
        String content,
        List<String> hashtags,
        LocalDateTime createdAt,
        String writerNickname,
        int likeCount,
        int commentCount,
        String thumbnailImageUrl
) {
    public static CurationPostsResponseDto from(CurationPost curationPost) {
        List<String> imageUrls = curationPost.getImages();
        return new CurationPostsResponseDto(
                curationPost.getTitle(),
                curationPost.getContent(),
                curationPost.getHashtags(),
                curationPost.getCreatedAt(),
                curationPost.getUser().getNickname(),
                curationPost.getLikeCount(),
                curationPost.getCommentCount(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst()
        );
    }
}


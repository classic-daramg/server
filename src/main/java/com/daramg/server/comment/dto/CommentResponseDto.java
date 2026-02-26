package com.daramg.server.comment.dto;

import com.daramg.server.comment.domain.Comment;

import java.time.Instant;
import java.util.List;

public record CommentResponseDto(
        Long id,
        String content,
        boolean isDeleted,
        int likeCount,
        int childCommentCount,
        Instant createdAt,
        String writerNickname,
        String writerProfileImage,
        Boolean isLiked,
        List<ChildCommentResponseDto> childComments
) {

    public static CommentResponseDto from(Comment comment, Boolean isLiked, List<ChildCommentResponseDto> childComments) {
        int childCommentCount = childComments != null
                ? (int) childComments.stream().filter(c -> !c.isDeleted()).count()
                : 0;

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.isDeleted(),
                comment.getLikeCount(),
                childCommentCount,
                comment.getCreatedAt(),
                comment.getUser().getNickname(),
                comment.getUser().getProfileImage(),
                isLiked,
                childComments
        );
    }

    public record ChildCommentResponseDto(
            Long id,
            String content,
            boolean isDeleted,
            int likeCount,
            Instant createdAt,
            String writerNickname,
            String writerProfileImage,
            Boolean isLiked
    ) {

        public static ChildCommentResponseDto from(Comment comment, Boolean isLiked) {
            return new ChildCommentResponseDto(
                    comment.getId(),
                    comment.getContent(),
                    comment.isDeleted(),
                    comment.getLikeCount(),
                    comment.getCreatedAt(),
                    comment.getUser().getNickname(),
                    comment.getUser().getProfileImage(),
                    isLiked
            );
        }
    }
}


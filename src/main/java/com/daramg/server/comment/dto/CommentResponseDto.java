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
        boolean isAi,
        String composerName,
        List<ChildCommentResponseDto> childComments
) {

    public static CommentResponseDto from(Comment comment, Boolean isLiked, List<ChildCommentResponseDto> childComments) {
        int childCommentCount = childComments != null
                ? (int) childComments.stream().filter(c -> !c.isDeleted()).count()
                : 0;

        String writerNickname = comment.isAi() ? null : comment.getUser().getNickname();
        String writerProfileImage = comment.isAi() ? null : comment.getUser().getProfileImage();
        String composerName = comment.isAi() && comment.getComposer() != null
                ? comment.getComposer().getKoreanName()
                : null;

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.isDeleted(),
                comment.getLikeCount(),
                childCommentCount,
                comment.getCreatedAt(),
                writerNickname,
                writerProfileImage,
                isLiked,
                comment.isAi(),
                composerName,
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
            Boolean isLiked,
            boolean isAi,
            String composerName
    ) {

        public static ChildCommentResponseDto from(Comment comment, Boolean isLiked) {
            String writerNickname = comment.isAi() ? null : comment.getUser().getNickname();
            String writerProfileImage = comment.isAi() ? null : comment.getUser().getProfileImage();
            String composerName = comment.isAi() && comment.getComposer() != null
                    ? comment.getComposer().getKoreanName()
                    : null;

            return new ChildCommentResponseDto(
                    comment.getId(),
                    comment.getContent(),
                    comment.isDeleted(),
                    comment.getLikeCount(),
                    comment.getCreatedAt(),
                    writerNickname,
                    writerProfileImage,
                    isLiked,
                    comment.isAi(),
                    composerName
            );
        }
    }
}


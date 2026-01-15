package com.daramg.server.comment.repository;

import com.daramg.server.comment.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
    void deleteAllByCommentId(Long commentId);
    List<CommentLike> findByCommentIdInAndUserId(Iterable<Long> commentIds, Long userId);
}

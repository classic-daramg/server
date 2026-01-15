package com.daramg.server.comment.repository;

import com.daramg.server.comment.domain.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user", "childComments", "childComments.user"})
    List<Comment> findByPostIdAndIsBlockedFalseOrderByCreatedAtAsc(Long postId);
}

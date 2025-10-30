package com.daramg.server.comment.presentation;

import com.daramg.server.comment.application.CommentService;
import com.daramg.server.comment.dto.CommentLikeResponseDto;
import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import com.daramg.server.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void createComment(@PathVariable Long postId,
                              @Valid @RequestBody CommentCreateDto request, User user) {
        commentService.createComment(postId, request, user);
    }

    @PostMapping("/comments/{parentCommentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public void createReply(
            @PathVariable Long parentCommentId,
            @Valid @RequestBody CommentReplyCreateDto request, User user
    ) {
        commentService.createReply(parentCommentId, request, user);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId, User user) {
        commentService.deleteComment(commentId, user);
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeResponseDto> toggleCommentLike(
            @PathVariable Long commentId, User user) {
        CommentLikeResponseDto responseDto = commentService.toggleCommentLike(commentId, user);
        return ResponseEntity.ok(responseDto);
    }
}

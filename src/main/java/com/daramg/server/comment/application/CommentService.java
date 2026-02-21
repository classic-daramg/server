package com.daramg.server.comment.application;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.domain.CommentLike;
import com.daramg.server.comment.dto.CommentLikeResponseDto;
import com.daramg.server.comment.repository.CommentLikeRepository;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.comment.exception.CommentErrorStatus;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.post.domain.Post;
import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.notification.event.NotificationEvent;
import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final EntityUtils entityUtils;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void createComment(Long postId, CommentCreateDto request, User user){
        Post post = entityUtils.getEntity(postId, Post.class);
        if (post.isBlocked()){
            throw new BusinessException(CommentErrorStatus.BLOCKED_POST);
        }

        Comment comment = Comment.of(
                post,
                user,
                request.getContent(),
                null
        );

        commentRepository.save(comment);
        post.incrementCommentCount();
        if (!post.getUser().getId().equals(user.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    post.getUser(), user, post, NotificationType.COMMENT
            ));
        }
    }

    public void createReply(Long commentId, CommentReplyCreateDto request, User user){
        Comment parentComment = entityUtils.getEntity(commentId, Comment.class);
        if (parentComment.isDeleted() || parentComment.isBlocked()){
            throw new BusinessException(CommentErrorStatus.BLOCKED_OR_DELETED_COMMENT_REPLY);
        }
        Post post = parentComment.getPost();
        if (post.isBlocked()){
            throw new BusinessException(CommentErrorStatus.BLOCKED_POST);
        }

        Comment reply = Comment.of(
                post,
                user,
                request.getContent(),
                parentComment
        );

        commentRepository.save(reply);
        post.incrementCommentCount();
        if (!parentComment.getUser().getId().equals(user.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    parentComment.getUser(), user, post, NotificationType.REPLY
            ));
        }
    }

    public CommentLikeResponseDto toggleCommentLike(Long commentId, User user){
        Comment comment = entityUtils.getEntity(commentId, Comment.class);
        if (comment.isDeleted() || comment.isBlocked()){
            throw new BusinessException(CommentErrorStatus.BLOCKED_OR_DELETED_COMMENT_LIKE);
        }

        boolean alreadyLiked = commentLikeRepository
                .existsByCommentIdAndUserId(commentId, user.getId());

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, user.getId());
            comment.decrementLikeCount();
            return new CommentLikeResponseDto(false, comment.getLikeCount());
        }

        commentLikeRepository.save(CommentLike.of(comment, user));
        comment.incrementLikeCount();
        if (!comment.getUser().getId().equals(user.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    comment.getUser(), user, comment.getPost(), NotificationType.COMMENT_LIKE
            ));
        }
        return new CommentLikeResponseDto(true, comment.getLikeCount());
    }

    public void deleteComment(Long commentId, User user){
        Comment comment = entityUtils.getEntity(commentId, Comment.class);

        if (comment.isDeleted()){
            throw new BusinessException(CommentErrorStatus.ALREADY_DELETED);
        }
        if (comment.getUser() == null || !comment.getUser().getId().equals(user.getId())){
            throw new BusinessException(CommentErrorStatus.NOT_COMMENT_AUTHOR);
        }

        comment.softDelete();
        commentLikeRepository.deleteAllByCommentId(commentId);
        comment.resetLikeCount();
    }
}

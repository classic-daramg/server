package com.daramg.server.aicomment.domain;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "ai_comment_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCommentJob extends BaseEntity<AiCommentJob> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id", nullable = false)
    private Composer composer;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private AiCommentJobTriggerType triggerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiCommentJobStatus status = AiCommentJobStatus.PENDING;

    private AiCommentJob(Post post, Composer composer, AiCommentJobTriggerType triggerType,
                         Comment parentComment, Instant scheduledAt) {
        this.post = post;
        this.composer = composer;
        this.triggerType = triggerType;
        this.parentComment = parentComment;
        this.scheduledAt = scheduledAt;
        this.status = AiCommentJobStatus.PENDING;
    }

    public static AiCommentJob of(Post post, Composer composer, AiCommentJobTriggerType triggerType,
                                  Comment parentComment, Instant scheduledAt) {
        return new AiCommentJob(post, composer, triggerType, parentComment, scheduledAt);
    }

    public void markInProgress() {
        this.status = AiCommentJobStatus.IN_PROGRESS;
    }

    public void markDone() {
        this.status = AiCommentJobStatus.DONE;
    }

    public void markFailed() {
        this.status = AiCommentJobStatus.FAILED;
    }
}

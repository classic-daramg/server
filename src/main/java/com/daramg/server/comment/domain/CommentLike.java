package com.daramg.server.comment.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_comment_user_like",
                        columnNames = {"comment_id", "user_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity<CommentLike> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private CommentLike(@NonNull Comment comment, @NonNull User user) {
        this.comment = comment;
        this.user = user;
    }

    public static CommentLike of(Comment comment, User user) {
        return new CommentLike(comment, user);
    }
}

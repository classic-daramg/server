package com.daramg.server.comment.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.post.domain.Post;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity<Comment> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", nullable = true)
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    private Comment(Post post, User user, String content, Comment parentComment) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.parentComment = parentComment;
    }

    public static Comment of(@NonNull Post post,
                             @NonNull User user,
                             @NonNull String content,
                             Comment parentComment) {
        return new Comment(post, user, content, parentComment);
    }

    public void incrementLikeCount() {
        likeCount++;
    }

    public void decrementLikeCount() {
        if(likeCount > 0){
            likeCount--;
        }
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void resetLikeCount() {
        this.likeCount = 0;
    }
}

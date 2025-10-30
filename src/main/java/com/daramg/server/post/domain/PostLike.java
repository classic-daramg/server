package com.daramg.server.post.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_post_user_like",
                        columnNames = {"post_id", "user_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity<PostLike> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public static PostLike of(@NonNull Post post, @NonNull User user) {
        return new PostLike(post, user);
    }
}

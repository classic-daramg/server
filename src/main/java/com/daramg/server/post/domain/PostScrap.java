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
@Table(name = "post_scraps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_post_user_scrap",
                        columnNames = {"post_id", "user_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostScrap extends BaseEntity<PostScrap> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private PostScrap(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public static PostScrap of(@NonNull Post post, @NonNull User user) {
        return new PostScrap(post, user);
    }
}

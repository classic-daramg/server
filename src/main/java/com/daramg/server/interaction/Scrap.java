package com.daramg.server.interaction;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.post.domain.Post;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "scraps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_post_user_scrap",
                        columnNames = {"post_id", "user_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity<Scrap> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Scrap(@NonNull Post post, @NonNull User user) {
        this.post = post;
        this.user = user;
    }
}

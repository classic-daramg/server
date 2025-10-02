package com.daramg.server.domain.composer;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.domain.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "composer_posts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_composer_post",
                        columnNames = {"composer_id", "post_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComposerPost extends BaseEntity<ComposerPost> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id", nullable = false)
    private Composer composer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private ComposerPost(Composer composer, Post post) {
        this.composer = composer;
        this.post = post;
    }
}

package com.daramg.server.composer.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "composer_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_composer_user_like",
                        columnNames = {"composer_id", "user_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComposerLike extends BaseEntity<ComposerLike> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id", nullable = false)
    private Composer composer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ComposerLike(Composer composer, User user) {
        this.composer = composer;
        this.user = user;
    }

    public static ComposerLike of(@NonNull Composer composer, @NonNull User user) {
        return new ComposerLike(composer, user);
    }
}

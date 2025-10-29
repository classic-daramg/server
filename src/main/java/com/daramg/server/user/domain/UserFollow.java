package com.daramg.server.user.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_follower_followed",
                        columnNames = {"follower_id", "followed_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFollow extends BaseEntity<UserFollow> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    private UserFollow(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
    }

    public static UserFollow of(User follower, User followed) {
        return new UserFollow(follower, followed);
    }
}


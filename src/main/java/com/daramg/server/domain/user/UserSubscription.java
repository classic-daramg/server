package com.daramg.server.domain.user;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_subscriber_followed",
                        columnNames = {"subscriber_id", "followed_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSubscription extends BaseEntity<UserSubscription> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    private UserSubscription(User subscriber, User followed) {
        this.subscriber = subscriber;
        this.followed = followed;
    }
}


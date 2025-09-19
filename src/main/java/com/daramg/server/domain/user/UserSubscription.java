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
                        name = "uc_subscriber_follower",
                        columnNames = {"subscriber_id", "follower_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSubscription extends BaseEntity<UserSubscription> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    private UserSubscription(User subscriber, User follower) {
        this.subscriber = subscriber;
        this.follower = follower;
    }
}


package com.daramg.server.notification.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.post.domain.Post;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity<Notification> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    private Notification(User receiver, User sender, Post post, NotificationType type) {
        this.receiver = receiver;
        this.sender = sender;
        this.post = post;
        this.type = type;
    }

    public static Notification of(@NonNull User receiver,
                                   @NonNull User sender,
                                   @NonNull Post post,
                                   @NonNull NotificationType type) {
        return new Notification(receiver, sender, post, type);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}

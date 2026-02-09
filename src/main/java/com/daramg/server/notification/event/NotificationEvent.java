package com.daramg.server.notification.event;

import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.post.domain.Post;
import com.daramg.server.user.domain.User;

public record NotificationEvent(
        User receiver,
        User sender,
        Post post,
        NotificationType type
) {
}

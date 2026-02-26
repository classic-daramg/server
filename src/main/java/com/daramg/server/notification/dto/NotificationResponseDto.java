package com.daramg.server.notification.dto;

import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.domain.NotificationType;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        String senderNickname,
        String senderProfileImage,
        Long postId,
        String postTitle,
        NotificationType type,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getSender().getNickname(),
                notification.getSender().getProfileImage(),
                notification.getPost().getId(),
                notification.getPost().getTitle(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

package com.daramg.server.notification.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notification.exception.NotificationErrorStatus;
import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EntityUtils entityUtils;

    public void markAsRead(Long notificationId, User user) {
        Notification notification = entityUtils.getEntity(notificationId, Notification.class);
        validateReceiver(notification, user);
        notification.markAsRead();
    }

    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByReceiverId(user.getId());
    }

    public void delete(Long notificationId, User user) {
        Notification notification = entityUtils.getEntity(notificationId, Notification.class);
        validateReceiver(notification, user);
        notificationRepository.delete(notification);
    }

    private void validateReceiver(Notification notification, User user) {
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new BusinessException(NotificationErrorStatus.NOT_OWN_NOTIFICATION);
        }
    }
}

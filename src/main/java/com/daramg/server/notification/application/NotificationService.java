package com.daramg.server.notification.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.event.NotificationEvent;
import com.daramg.server.notification.exception.NotificationErrorStatus;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.post.domain.Post;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_NOTIFICATIONS = 100;

    private final NotificationRepository notificationRepository;
    private final EntityUtils entityUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFromEvent(NotificationEvent event) {
        User receiver = entityUtils.getEntity(event.receiver().getId(), User.class);
        User sender = entityUtils.getEntity(event.sender().getId(), User.class);
        Post post = entityUtils.getEntity(event.post().getId(), Post.class);

        Notification notification = Notification.of(receiver, sender, post, event.type());
        notificationRepository.save(notification);
        trimNotifications(receiver.getId());
    }

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

    private void trimNotifications(Long receiverId) {
        if (notificationRepository.countByReceiverId(receiverId) <= MAX_NOTIFICATIONS) {
            return;
        }

        List<Long> keepIds = notificationRepository.findRecentIdsByReceiverId(
                receiverId,
                PageRequest.of(0, MAX_NOTIFICATIONS)
        );

        if (!keepIds.isEmpty()) {
            notificationRepository.deleteByReceiverIdAndIdNotIn(receiverId, keepIds);
        }
    }

    private void validateReceiver(Notification notification, User user) {
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new BusinessException(NotificationErrorStatus.NOT_OWN_NOTIFICATION);
        }
    }
}

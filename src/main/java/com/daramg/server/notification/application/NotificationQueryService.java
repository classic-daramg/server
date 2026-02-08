package com.daramg.server.notification.application;

import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDto> getNotifications(User user) {
        return notificationRepository.findAllByReceiverIdWithSenderAndPost(user.getId())
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(user.getId());
    }
}

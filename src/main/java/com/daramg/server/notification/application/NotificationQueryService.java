package com.daramg.server.notification.application;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.notification.repository.NotificationQueryRepository;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationQueryRepository notificationQueryRepository;
    private final PagingUtils pagingUtils;

    public PageResponseDto<NotificationResponseDto> getNotifications(User user, PageRequestDto request) {
        List<Notification> notifications = notificationQueryRepository
                .getNotificationsWithPaging(user.getId(), request);

        return pagingUtils.createPageResponse(
                notifications,
                request.getValidatedSize(),
                NotificationResponseDto::from,
                Notification::getCreatedAt,
                Notification::getId
        );
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countUnreadByReceiverIdSince(user.getId(), Instant.now().minus(30, ChronoUnit.DAYS));
    }
}

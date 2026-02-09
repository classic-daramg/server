package com.daramg.server.notification.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.notification.domain.Notification;

import java.util.List;

public interface NotificationQueryRepository {
    List<Notification> getNotificationsWithPaging(Long receiverId, PageRequestDto pageRequest);
}

package com.daramg.server.notification.presentation;

import com.daramg.server.notification.application.NotificationQueryService;
import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public List<NotificationResponseDto> getNotifications(User user) {
        return notificationQueryService.getNotifications(user);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(User user) {
        return notificationQueryService.getUnreadCount(user);
    }
}

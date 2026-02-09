package com.daramg.server.notification.presentation;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.notification.application.NotificationQueryService;
import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public PageResponseDto<NotificationResponseDto> getNotifications(
            User user,
            @ModelAttribute PageRequestDto request
    ) {
        return notificationQueryService.getNotifications(user, request);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(User user) {
        return notificationQueryService.getUnreadCount(user);
    }
}

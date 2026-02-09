package com.daramg.server.notification.presentation;

import com.daramg.server.notification.application.NotificationService;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.OK)
    public void markAsRead(@PathVariable Long notificationId, User user) {
        notificationService.markAsRead(notificationId, user);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    public void markAllAsRead(User user) {
        notificationService.markAllAsRead(user);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long notificationId, User user) {
        notificationService.delete(notificationId, user);
    }
}

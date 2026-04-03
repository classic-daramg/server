package com.daramg.server.notification.event;

import com.daramg.server.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        if (event.receiver().getId().equals(event.sender().getId())) {
            return;
        }

        try {
            notificationService.createFromEvent(event);
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to process notification event. receiverId={}, senderId={}, postId={}, type={}",
                    event.receiver().getId(),
                    event.sender().getId(),
                    event.post().getId(),
                    event.type(),
                    e
            );
        }
    }
}

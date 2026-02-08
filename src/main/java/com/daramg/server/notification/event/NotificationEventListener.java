package com.daramg.server.notification.event;

import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        if (event.receiver().getId().equals(event.sender().getId())) {
            return;
        }

        Notification notification = Notification.of(
                event.receiver(),
                event.sender(),
                event.post(),
                event.type()
        );

        notificationRepository.save(notification);
    }
}

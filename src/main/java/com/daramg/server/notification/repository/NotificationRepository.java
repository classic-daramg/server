package com.daramg.server.notification.repository;

import com.daramg.server.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false AND n.createdAt >= :since")
    long countUnreadByReceiverIdSince(Long receiverId, Instant since);

    long countByReceiverId(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(Long receiverId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.receiver.id = :receiverId AND n.id NOT IN " +
            "(SELECT n2.id FROM Notification n2 WHERE n2.receiver.id = :receiverId ORDER BY n2.createdAt DESC LIMIT 100)")
    void deleteOldestByReceiverIdExceedingLimit(Long receiverId);
}

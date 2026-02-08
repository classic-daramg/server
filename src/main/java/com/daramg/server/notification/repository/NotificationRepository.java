package com.daramg.server.notification.repository;

import com.daramg.server.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            join fetch n.sender
            join fetch n.post
            where n.receiver.id = :receiverId
            order by n.createdAt desc
            """)
    List<Notification> findAllByReceiverIdWithSenderAndPost(Long receiverId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(Long receiverId);
}

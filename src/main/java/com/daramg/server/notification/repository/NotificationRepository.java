package com.daramg.server.notification.repository;

import com.daramg.server.notification.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false AND n.createdAt >= :since")
    long countUnreadByReceiverIdSince(Long receiverId, Instant since);

    long countByReceiverId(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(Long receiverId);

    @Query("SELECT n.id FROM Notification n WHERE n.receiver.id = :receiverId ORDER BY n.createdAt DESC, n.id DESC")
    List<Long> findRecentIdsByReceiverId(Long receiverId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.receiver.id = :receiverId AND n.id NOT IN :keepIds")
    void deleteByReceiverIdAndIdNotIn(Long receiverId, Collection<Long> keepIds);
}

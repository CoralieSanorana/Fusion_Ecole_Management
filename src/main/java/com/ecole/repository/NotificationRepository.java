package com.ecole.repository;

import com.ecole.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId IS NULL OR n.userId = ?1
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findVisibleForUser(Integer userId);

    @Query("""
            SELECT n FROM Notification n
            WHERE (n.userId IS NULL OR n.userId = ?1) AND n.estLu = false
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findUnreadVisibleForUser(Integer userId);

    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.userId IS NULL OR n.userId = ?1
            """)
    long countVisibleForUser(Integer userId);

    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE (n.userId IS NULL OR n.userId = ?1) AND n.estLu = false
            """)
    long countUnreadVisibleForUser(Integer userId);
}

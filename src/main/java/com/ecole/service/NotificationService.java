package com.ecole.service;

import com.ecole.entity.Notification;
import com.ecole.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findVisibleForUser(Integer userId) {
        return notificationRepository.findVisibleForUser(userId);
    }

    public List<Notification> findTopVisibleForUser(Integer userId, int limit) {
        return notificationRepository.findVisibleForUser(userId).stream()
                .limit(limit)
                .toList();
    }

    public long countVisibleForUser(Integer userId) {
        return notificationRepository.countVisibleForUser(userId);
    }

    public long countUnreadVisibleForUser(Integer userId) {
        return notificationRepository.countUnreadVisibleForUser(userId);
    }

    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }
}

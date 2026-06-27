package com.ecole.service;

import com.ecole.entity.NotificationType;
import com.ecole.repository.NotificationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationTypeService {

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    public List<NotificationType> findAll() {
        return notificationTypeRepository.findAll();
    }

    public Optional<NotificationType> findById(Long id) {
        return notificationTypeRepository.findById(id);
    }

    public NotificationType save(NotificationType notificationType) {
        return notificationTypeRepository.save(notificationType);
    }

    public void deleteById(Long id) {
        notificationTypeRepository.deleteById(id);
    }
}

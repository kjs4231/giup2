package com.example.giup2.service;

import com.example.giup2.entity.ProductUserNotification;
import com.example.giup2.repository.ProductUserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductUserNotificationService {

    private final ProductUserNotificationRepository userNotificationRepository;

    public ProductUserNotificationService(ProductUserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public List<ProductUserNotification> findActiveNotifications(Long productId) {
        return userNotificationRepository.findByProductIdAndActiveIsTrue(productId);
    }
}

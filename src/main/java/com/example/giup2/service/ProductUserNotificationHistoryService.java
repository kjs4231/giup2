package com.example.giup2.service;

import com.example.giup2.entity.Product;
import com.example.giup2.entity.ProductUserNotificationHistory;
import com.example.giup2.repository.ProductUserNotificationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductUserNotificationHistoryService {

    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    public ProductUserNotificationHistoryService(ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    @Transactional
    public void createUserNotificationHistory(Product product, Long userId) {
        ProductUserNotificationHistory history = new ProductUserNotificationHistory(product, userId, product.getRestockCount(), LocalDateTime.now());
        userNotificationHistoryRepository.save(history);
    }
}

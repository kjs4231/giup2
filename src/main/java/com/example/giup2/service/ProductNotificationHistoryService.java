package com.example.giup2.service;

import com.example.giup2.entity.NotificationStatus;
import com.example.giup2.entity.Product;
import com.example.giup2.entity.ProductNotificationHistory;
import com.example.giup2.repository.ProductNotificationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProductNotificationHistoryService {

    private final ProductNotificationHistoryRepository notificationHistoryRepository;

    public ProductNotificationHistoryService(ProductNotificationHistoryRepository notificationHistoryRepository) {
        this.notificationHistoryRepository = notificationHistoryRepository;
    }

    @Transactional
    public ProductNotificationHistory create(Product product, int restockCount) {
        ProductNotificationHistory history = new ProductNotificationHistory(product, restockCount, NotificationStatus.IN_PROGRESS);
        return notificationHistoryRepository.save(history);
    }

    @Transactional
    public void updateStatus(ProductNotificationHistory history, NotificationStatus status) {
        history.setStatus(status);
        notificationHistoryRepository.save(history);
    }

    @Transactional
    public ProductNotificationHistory findLatestByStatus(Long productId, NotificationStatus status) {
        return notificationHistoryRepository.findLatestByProductIdAndStatus(productId, status);
    }
}

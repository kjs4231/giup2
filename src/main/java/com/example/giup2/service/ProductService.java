package com.example.giup2.service;

import com.example.giup2.entity.NotificationStatus;
import com.example.giup2.entity.Product;
import com.example.giup2.entity.ProductNotificationHistory;
import com.example.giup2.entity.ProductUserNotification;
import com.example.giup2.repository.ProductRepository;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private final ProductNotificationHistoryService notificationHistoryService;
    private final ProductUserNotificationService userNotificationService;
    private final ProductUserNotificationHistoryService userNotificationHistoryService;
    private final ProductRepository productRepository;

    private final RateLimiter rateLimiter = RateLimiter.create(500.0);

    private final ConcurrentHashMap<Long, Boolean> notifyList = new ConcurrentHashMap<>();

    public ProductService(ProductRepository productRepository,
                          ProductNotificationHistoryService notificationHistoryService,
                          ProductUserNotificationService userNotificationService,
                          ProductUserNotificationHistoryService userNotificationHistoryService) {
        this.productRepository = productRepository;
        this.notificationHistoryService = notificationHistoryService;
        this.userNotificationService = userNotificationService;
        this.userNotificationHistoryService = userNotificationHistoryService;
    }

    @Transactional
    public void restock(Long productId) {

        if (notifyList.putIfAbsent(productId, true) != null) {
            System.out.println("이미 알림이 전송 중입니다.");
            return;
        }

        ProductNotificationHistory notificationHistory = null;

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다."));

            // 재입고 회차 증가
            product.setRestockCount(product.getRestockCount() + 1);
            productRepository.save(product);

            // 알림 상태를 IN_PROGRESS로 저장
            notificationHistory = notificationHistoryService.create(product, product.getRestockCount());

            // 알림을 설정한 사용자 목록 조회
            List<ProductUserNotification> userNotifications = userNotificationService.findActiveNotifications(productId);

            // RateLimite
            for (ProductUserNotification notification : userNotifications) {
                rateLimiter.acquire();
                processAlarm(notification, product, notificationHistory);
            }

            notificationHistoryService.updateStatus(notificationHistory, NotificationStatus.COMPLETED);

        } catch (Exception e) {
            if (notificationHistory != null) {
                notificationHistoryService.updateStatus(notificationHistory, NotificationStatus.CANCELED_BY_ERROR);
            }
            throw new RuntimeException("알림 전송 중 오류 발생", e);
        } finally {
            notifyList.remove(productId);
        }
    }

    private void processAlarm(ProductUserNotification notification, Product product, ProductNotificationHistory notificationHistory) {
        Long userId = notification.getUserId();

        // 재고가 소진되면 알림을 중단하고 상태를 업데이트
        if (product.getStock() <= 0) {
            notificationHistoryService.updateStatus(notificationHistory, NotificationStatus.CANCELED_BY_SOLD_OUT);
            System.out.println("재고가 모두 소진되었습니다.");
            return;
        }

        // 사용자 알림 히스토리 생성
        userNotificationHistoryService.createUserNotificationHistory(product, userId);

        // 재고 감소
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        // 마지막 유저 ID 업데이트
        notificationHistory.setLastNotifiedUserId(userId);
    }

    @Transactional
    public void retry(Long productId) {

        ProductNotificationHistory notificationHistory = notificationHistoryService.findLatestByStatus(
                productId, NotificationStatus.CANCELED_BY_ERROR);

        if (notificationHistory == null) {
            throw new IllegalStateException("미처리된 알람이 없습니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다."));

        List<ProductUserNotification> userNotifications = userNotificationService.findActiveNotifications(productId);

        boolean findLastUser = false;

        for (ProductUserNotification notification : userNotifications) {
            if (notification.getUserId().equals(notificationHistory.getLastNotifiedUserId())) {
                findLastUser = true;
                continue;
            }

            if (findLastUser) {
                processAlarm(notification, product, notificationHistory);
            }
        }

        notificationHistoryService.updateStatus(notificationHistory, NotificationStatus.COMPLETED);
    }
}

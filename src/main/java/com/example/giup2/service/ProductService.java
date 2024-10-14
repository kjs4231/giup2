package com.example.giup2.service;

import com.example.giup2.entity.*;
import com.example.giup2.repository.ProductNotificationHistoryRepository;
import com.example.giup2.repository.ProductRepository;
import com.example.giup2.repository.ProductUserNotificationHistoryRepository;
import com.example.giup2.repository.ProductUserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductNotificationHistoryRepository notificationHistoryRepository;
    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    private final ConcurrentHashMap<Long, Boolean> notifyList = new ConcurrentHashMap<>();

    public ProductService(ProductRepository productRepository,
                          ProductUserNotificationRepository productUserNotificationRepository,
                          ProductNotificationHistoryRepository notificationHistoryRepository,
                          ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.productRepository = productRepository;
        this.productUserNotificationRepository = productUserNotificationRepository;
        this.notificationHistoryRepository = notificationHistoryRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    @Transactional
    public void notifyUsersOnRestock(Long productId) {

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
            notificationHistory = new ProductNotificationHistory(product, product.getRestockCount(), NotificationStatus.IN_PROGRESS);
            notificationHistoryRepository.save(notificationHistory);

            // 알림을 설정한 사용자 목록 조회
            List<ProductUserNotification> userNotifications = productUserNotificationRepository.findByProductId(productId);

            for (ProductUserNotification notification : userNotifications) {
                processNotification(notification, product, notificationHistory);
            }

            notificationHistory.setStatus(NotificationStatus.COMPLETED);
            notificationHistoryRepository.save(notificationHistory);

        } catch (Exception e) {
            if (notificationHistory != null) {
                notificationHistory.setStatus(NotificationStatus.CANCELED_BY_ERROR);
                notificationHistoryRepository.save(notificationHistory);
            }
            throw new RuntimeException("알림 전송 중 오류 발생", e);
        } finally {
            notifyList.remove(productId);
        }
    }

    private void processNotification(ProductUserNotification notification, Product product, ProductNotificationHistory notificationHistory) {
        Long userId = notification.getUserId();

        // 재고가 소진되었으면 알림 상태를 CANCELED_BY_SOLD_OUT으로 저장하고 중단
        if (product.getStock() <= 0) {
            System.out.println("재고가 모두 소진되었습니다.");
            notificationHistory.setStatus(NotificationStatus.CANCELED_BY_SOLD_OUT);
            notificationHistoryRepository.save(notificationHistory);
            return;
        }

        // 사용자 알림 히스토리 생성
        ProductUserNotificationHistory userNotificationHistory = new ProductUserNotificationHistory(
                product, userId, product.getRestockCount(), LocalDateTime.now()
        );
        userNotificationHistoryRepository.save(userNotificationHistory);

        // 재고 감소
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        // 마지막 유저 ID 업데이트
        notificationHistory.setLastNotifiedUserId(userId);
    }
}

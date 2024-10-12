package com.example.giup2.service;

import com.example.giup2.entity.*;
import com.example.giup2.repository.ProductNotificationHistoryRepository;
import com.example.giup2.repository.ProductRepository;
import com.example.giup2.repository.ProductUserNotificationHistoryRepository;
import com.example.giup2.repository.ProductUserNotificationRepository;
import com.google.common.util.concurrent.RateLimiter;
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

        // 중복된 알림 전송을 방지하기 위해, 이미 전송 중이면 종료
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
            notificationHistory = new ProductNotificationHistory(
                    product,
                    product.getRestockCount(),
                    NotificationStatus.IN_PROGRESS
            );
            notificationHistoryRepository.save(notificationHistory);

            // 알림을 설정한 사용자 목록 조회
            List<ProductUserNotification> userNotifications = productUserNotificationRepository.findByProductId(productId);

            // RateLimiter 생성
            RateLimiter rateLimiter = RateLimiter.create(500.0);

            // 사용자에게 알림 전송
            for (ProductUserNotification notification : userNotifications) {
                rateLimiter.acquire();  // 500개 제한
                processNotification(notification, product);
            }

            // 알림 전송 완료 후 상태를 COMPLETED로 설정
            notificationHistory.setStatus(NotificationStatus.COMPLETED);
            notificationHistoryRepository.save(notificationHistory);

        } catch (Exception e) {
            // 알림 전송 중 오류 발생 시 상태를 CANCELED_BY_ERROR로 설정
            if (notificationHistory != null) {
                notificationHistory.setStatus(NotificationStatus.CANCELED_BY_ERROR);
                notificationHistoryRepository.save(notificationHistory);
            }
            throw new RuntimeException("알림 전송 중 오류 발생", e);

        } finally {
            // 알림 전송이 끝나면 캐시에서 해당 상품 ID를 제거
            notifyList.remove(productId);
        }
    }


    private void processNotification(ProductUserNotification notification, Product product) {
        Long userId = notification.getUserId();

        // 재고가 소진되면 알림 전송 중단
        if (product.getStock() <= 0) {
            System.out.println("재고가 모두 소진되었습니다.");
            return;
        }

        // 사용자 알림 히스토리 생성 및 저장
        ProductUserNotificationHistory userNotificationHistory = new ProductUserNotificationHistory(
                product,
                userId,
                product.getRestockCount(),
                LocalDateTime.now()
        );
        userNotificationHistoryRepository.save(userNotificationHistory);

        // 재고 감소
        product.setStock(product.getStock() - 1);
        productRepository.save(product);
    }
}
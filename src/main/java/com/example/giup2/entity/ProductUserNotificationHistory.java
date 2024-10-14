package com.example.giup2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_user_notification_history")
public class ProductUserNotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Long userId;
    private int restockRound;
    private LocalDateTime notificationSentAt;

    public ProductUserNotificationHistory(Product product, Long userId, int restockRound, LocalDateTime notificationSentAt) {
        this.product = product;
        this.userId = userId;
        this.restockRound = restockRound;
        this.notificationSentAt = notificationSentAt;
    }
}

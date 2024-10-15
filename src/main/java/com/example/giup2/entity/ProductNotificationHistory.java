package com.example.giup2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_notification_history", indexes = {
        @Index(name = "idx_product_status", columnList = "product_id, status")
})
public class ProductNotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int restockRound;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Long lastNotifiedUserId;

    public ProductNotificationHistory(Product product, int restockRound, NotificationStatus status) {
        this.product = product;
        this.restockRound = restockRound;
        this.status = status;
    }

    public void setLastNotifiedUserId(Long lastNotifiedUserId) {
        this.lastNotifiedUserId = lastNotifiedUserId;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
}

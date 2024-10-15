package com.example.giup2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_user_notification", indexes = {
        @Index(name = "idx_product_id_active", columnList = "product_id, is_active")
})
public class ProductUserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Long userId;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

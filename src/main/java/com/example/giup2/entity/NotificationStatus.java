package com.example.giup2.entity;

public enum NotificationStatus {
    IN_PROGRESS,        // 발송 중
    CANCELED_BY_SOLD_OUT, // 품절로 인한 중단
    CANCELED_BY_ERROR,   // 에러로 인한 중단
    COMPLETED            // 알림 전송 완료
}

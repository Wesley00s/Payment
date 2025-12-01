package com.example.payment.dto;

import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID orderId,
    String transactionId,
    PaymentStatus status,
    BigDecimal amount,
    LocalDateTime lastUpdate
) {
    public static PaymentResponse fromDTO(Payment entity) {
        return new PaymentResponse(
                entity.getOrderId(),
                entity.getTransactionId(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt()
        );
    }
}
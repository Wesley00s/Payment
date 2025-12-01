package com.example.payment.dto;

import com.example.payment.model.PaymentStatus;
import java.util.UUID;

public record PaymentConcludedEvent(
    UUID orderId,
    String transactionId,
    PaymentStatus status,
    String message
) {}
package com.example.payment.dto;

import com.example.payment.strategy.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID orderId,
        BigDecimal amount,
        PaymentType type,
        String customerId,
        String cardToken
) {
}
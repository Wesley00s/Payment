package com.example.payment.strategy;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.model.Payment;

public interface PaymentStrategy {
    String processPayment(PaymentRequest paymentRequest);
    void refundPayment(Payment payment);
    PaymentType getPaymentType();
}

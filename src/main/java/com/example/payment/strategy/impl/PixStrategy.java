package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.model.Payment;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.PaymentType;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PixStrategy implements PaymentStrategy {

    @Override
    public String processPayment(PaymentRequest request) {
        log.info("Gerando QR Code PIX. Valor: {}", request.amount());
        return "";
    }

    @Override
    public void refundPayment(Payment payment) {

    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.PIX;
    }
}
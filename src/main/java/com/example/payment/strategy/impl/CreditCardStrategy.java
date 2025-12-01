package com.example.payment.strategy.impl;


import com.example.payment.dto.PaymentRequest;
import com.example.payment.integration.stripe.StripeClient;
import com.example.payment.integration.stripe.dto.GatewayChargeResponse;
import com.example.payment.model.Payment;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditCardStrategy implements PaymentStrategy {

    private final StripeClient stripeClient;

    @Value("${integration.stripe.secret-key}")
    private String apiKey;

    @Override
    public String processPayment(PaymentRequest request) {
        log.info("Iniciando integração com Gateway para pedido {}", request.orderId());

        try {
            String tokenToUse = (request.cardToken() != null && !request.cardToken().isEmpty())
                    ? request.cardToken()
                    : "tok_visa";

            GatewayChargeResponse response = stripeClient.createCharge(
                    apiKey,
                    tokenToUse,
                    request.amount().multiply(BigDecimal.valueOf(100)).longValue(),
                    "BRL",
                    "Pedido " + request.orderId()
            );

            if ("succeeded".equals(response.status())) {
                log.info("Pagamento aprovado! ID Transação: {}", response.id());
                return response.id();
            } else {
                log.error("Pagamento recusado: {}", response.failureMessage());
                throw new RuntimeException("Pagamento Recusado: " + response.failureMessage());
            }

        } catch (Exception e) {
            log.error("Erro na Strategy", e);
            throw e;
        }
    }

    @Override
    public void refundPayment(Payment payment) {
        log.info("Iniciando reembolso para o pagamento {}", payment.getOrderId());

        try {
            var response = stripeClient.createRefund(apiKey, payment.getTransactionId());

            if ("succeeded".equals(response.status()) || "pending".equals(response.status())) {
                log.info("Reembolso efetuado no Gateway. ID: {}", response.id());
            } else {
                throw new RuntimeException("Falha ao reembolsar: " + response.failureMessage());
            }
        } catch (Exception e) {
            log.error("Erro no reembolso", e);
            throw e;
        }
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.CREDIT_CARD;
    }
}
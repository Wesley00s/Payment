package com.example.payment.factory;

import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.PaymentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentFactory {
    private final Map<PaymentType, PaymentStrategy> strategies;

    public PaymentFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(java.util.stream.Collectors
                        .toMap(PaymentStrategy::getPaymentType, strategy -> strategy));
    }

    public PaymentStrategy getStrategy(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Payment method not supported: " + type);
        }
        return strategy;
    }
}

package com.example.payment.integration.stripe;

import com.example.payment.integration.stripe.dto.GatewayChargeResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StripeClientFallbackFactory implements FallbackFactory<StripeClient> {

    @Override
    public StripeClient create(Throwable cause) {
        return new StripeClient() {

            @Override
            public GatewayChargeResponse createCharge(String token, String cardToken, Long amount, String currency, String description) {
                log.error("CIRCUIT BREAKER [Charge]: Falha ao chamar Stripe. Erro: {}", cause.getMessage());
                return new GatewayChargeResponse(null, "SERVICE_UNAVAILABLE", "O serviço de pagamento está instável (Fallback).");
            }

            @Override
            public GatewayChargeResponse createRefund(String token, String transactionId) {
                log.error("CIRCUIT BREAKER [Refund]: Falha ao chamar Stripe. Erro: {}", cause.getMessage());
                return new GatewayChargeResponse(null, "SERVICE_UNAVAILABLE", "Não foi possível processar o reembolso (Fallback).");
            }
        };
    }
}
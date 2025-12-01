package com.example.payment.integration.stripe;

import com.example.payment.integration.stripe.dto.GatewayChargeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "stripeClient",
        url = "${integration.stripe.url}",
        fallbackFactory = StripeClientFallbackFactory.class
)
public interface StripeClient {
    @PostMapping(value = "/v1/charges", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GatewayChargeResponse createCharge(
            @RequestHeader("Authorization") String token,
            @RequestParam("source") String cardToken,
            @RequestParam("amount") Long amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    );

    @PostMapping(value = "/v1/refunds", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GatewayChargeResponse createRefund(
            @RequestHeader("Authorization") String token,
            @RequestParam("charge") String transactionId
    );

}
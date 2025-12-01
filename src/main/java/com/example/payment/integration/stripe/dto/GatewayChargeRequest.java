package com.example.payment.integration.stripe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GatewayChargeRequest(
    @JsonProperty("source_id") String cardToken,
    @JsonProperty("amount_cents") Long amountCents,
    @JsonProperty("currency") String currency,
    @JsonProperty("capture") Boolean capture
) {}
package com.example.payment.integration.stripe.dto;

public record GatewayChargeResponse(
    String id,
    String status,
    String failureMessage
) {}
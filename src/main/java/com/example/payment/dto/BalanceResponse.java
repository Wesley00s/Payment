package com.example.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record BalanceResponse(
    @JsonProperty("gross_revenue")
    BigDecimal grossRevenue,
    
    @JsonProperty("refunded_amount")
    BigDecimal refundedAmount,
    
    @JsonProperty("net_revenue")
    BigDecimal netRevenue
) {}
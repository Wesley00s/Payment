package com.example.payment.controller;

import com.example.payment.config.RabbitConfig;
import com.example.payment.dto.*;
import com.example.payment.model.PaymentStatus;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final RabbitTemplate rabbitTemplate;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Void> initiatePayment(@RequestBody PaymentRequest request) {
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_PAYMENT, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable UUID orderId) {
        return paymentService.findByOrderId(orderId)
                .map(PaymentResponse::fromDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundPayment(@PathVariable UUID orderId) {
        paymentService.processRefund(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        BigDecimal total = paymentService.calculateTotalRevenue();
        BigDecimal refunded = paymentService.calculateTotalRefunds();
        BigDecimal net = total.subtract(refunded);

        var response = new BalanceResponse(total, refunded, net);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> listPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        val payments = paymentService.findAll(status, page, pageSize);
        var paymentList = payments.stream()
                .map(PaymentResponse::fromDTO)
                .collect(Collectors.toList());

        var pageResponse = new ApiResponse<>(
                paymentList,
                new PaginationResponse(
                        payments.getNumber(),
                        payments.getSize(),
                        payments.getTotalElements(),
                        payments.getTotalPages()
                )
        );
        return ResponseEntity.ok(pageResponse);
    }
}
package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.strategy.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentFactory paymentFactory;

    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return paymentRepository.calculateTotalRevenue();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRefunds() {
        return paymentRepository.calculateTotalRefunds();
    }

    @Transactional
    public void processSecurePayment(PaymentRequest request) {

        if (paymentRepository.findByOrderId(request.orderId()).isPresent()) {
            return;
        }

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.saveAndFlush(payment);

        try {
            var strategy = paymentFactory.getStrategy(request.type());
            String transactionId = strategy.processPayment(request);

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);


        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw e;
        }
    }

    @Transactional
    public void processRefund(UUID orderId) {
        var payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Só é possível reembolsar pagamentos aprovados.");
        }

        var strategy = paymentFactory.getStrategy(PaymentType.CREDIT_CARD);
        strategy.refundPayment(payment);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Page<Payment> findAll(PaymentStatus status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments;
        if (status != null) {
            payments = paymentRepository.findByStatus(status, pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }
        return payments;
    }
}
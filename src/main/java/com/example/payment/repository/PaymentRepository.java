package com.example.payment.repository;

import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(UUID orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status IN ('SUCCESS', 'REFUNDED')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'REFUNDED'")
    BigDecimal calculateTotalRefunds();
}
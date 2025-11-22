package com.example.cake.payment.repository;

import com.example.cake.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Find payment by VNPAY transaction reference
     */
    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);

    /**
     * Find payment by VNPAY transaction number
     */
    Optional<Payment> findByVnpTransactionNo(String vnpTransactionNo);

    /**
     * Find all payments by user
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);

    /**
     * Find payments by user and status
     */
    List<Payment> findByUserIdAndStatus(String userId, Payment.PaymentStatus status);
}


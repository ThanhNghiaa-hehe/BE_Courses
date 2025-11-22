package com.example.cake.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Payment entity - Track payment transactions
 */
@Document(collection = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id;

    private String userId;
    private String cartId;

    // VNPAY transaction info
    private Long amount;                    // Số tiền (VNĐ)
    private String vnpTxnRef;               // Mã giao dịch (payment ID)
    private String vnpTransactionNo;        // Mã giao dịch VNPAY
    private String vnpBankCode;             // Mã ngân hàng
    private String vnpResponseCode;         // Mã phản hồi (00 = success)
    private String vnpOrderInfo;            // Mô tả đơn hàng

    // Payment status
    private PaymentStatus status;           // PENDING, SUCCESS, FAILED, CANCELLED
    private PaymentMethod paymentMethod;    // VNPAY, MOMO, etc.

    // URLs
    private String returnUrl;
    private String ipnUrl;

    // Tracking
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    // Additional info
    private String ipAddress;
    private String locale;                  // vn hoặc en

    public enum PaymentStatus {
        PENDING,        // Chờ thanh toán
        SUCCESS,        // Thành công
        FAILED,         // Thất bại
        CANCELLED       // Đã hủy
    }

    public enum PaymentMethod {
        VNPAY,
        MOMO,
        BANK_TRANSFER
    }
}


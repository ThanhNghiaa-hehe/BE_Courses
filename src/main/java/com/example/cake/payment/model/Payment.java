package com.example.cake.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment entity - Track payment transactions
 * Updated: Removed cart/order dependency, stores course info directly
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

    // Course information - stored directly in payment
    private List<PaymentCourseItem> courses;  // List of courses being purchased

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

    /**
     * Payment Course Item - Store course info in payment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentCourseItem {
        private String courseId;
        private String title;
        private String thumbnailUrl;
        private Double price;              // Giá gốc
        private Double discountedPrice;    // Giá sau giảm
        private Integer discountPercent;   // % giảm giá
        private String instructorName;
        private String level;
    }
}


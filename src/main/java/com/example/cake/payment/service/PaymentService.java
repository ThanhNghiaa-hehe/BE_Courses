package com.example.cake.payment.service;

import com.example.cake.cart.model.Cart;
import com.example.cake.cart.repository.CartRepository;
import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.repository.UserProgressRepository;
import com.example.cake.payment.model.Payment;
import com.example.cake.payment.repository.PaymentRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment Service - Handle payment processing
 * Updated: Fixed field names and type conversions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final VNPayService vnPayService;
    private final UserProgressRepository userProgressRepository;

    /**
     * Create VNPAY payment
     */
    public ResponseMessage<Map<String, String>> createVNPayPayment(
            String userId,
            String orderInfo,
            String ipAddress
    ) {
        try {
            // Get user's cart
            Cart cart = cartRepository.findByUserId(userId).orElse(null);
            if (cart == null || cart.getItems().isEmpty()) {
                return new ResponseMessage<>(false, "Giỏ hàng trống", null);
            }

            // Calculate total price - đảm bảo là số nguyên
            double totalPriceDouble = cart.getItems().stream()
                    .filter(item -> item.getPrice() != null) // Filter null prices
                    .mapToDouble(item -> {
                        // Dùng discounted price nếu có, nếu không dùng price gốc
                        Double price = item.getDiscountedPrice() != null
                            ? item.getDiscountedPrice()
                            : item.getPrice();
                        return price != null ? price : 0.0;
                    })
                    .sum(); // Sum trả về primitive double

            long totalPrice = Math.round(totalPriceDouble); // Convert to long - round để tránh mất dữ liệu

            if (totalPrice <= 0) {
                return new ResponseMessage<>(false, "Tổng tiền không hợp lệ", null);
            }

            // Create payment record
            Payment payment = Payment.builder()
                    .userId(userId)
                    .cartId(cart.getId())
                    .amount(totalPrice)
                    .vnpOrderInfo(orderInfo)
                    .status(Payment.PaymentStatus.PENDING)
                    .paymentMethod(Payment.PaymentMethod.VNPAY)
                    .ipAddress(ipAddress)
                    .locale("vn")
                    .createdAt(LocalDateTime.now())
                    .build();

            payment = paymentRepository.save(payment);
            payment.setVnpTxnRef(payment.getId()); // Use payment ID as transaction ref
            paymentRepository.save(payment);

            // Create VNPAY payment URL
            String paymentUrl = vnPayService.createPaymentUrl(
                    payment.getId(),
                    payment.getAmount(),
                    orderInfo,
                    ipAddress
            );

            Map<String, String> result = new HashMap<>();
            result.put("paymentUrl", paymentUrl);
            result.put("paymentId", payment.getId());

            log.info("Created payment {} for user {}, amount: {} VND",
                    payment.getId(), userId, payment.getAmount());

            return new ResponseMessage<>(true, "Tạo link thanh toán thành công", result);

        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            return new ResponseMessage<>(false, "Lỗi tạo thanh toán: " + e.getMessage(), null);
        }
    }

    /**
     * Process VNPAY return/IPN callback
     */
    public ResponseMessage<Map<String, Object>> processVNPayReturn(Map<String, String> params) {
        try {
            // Verify signature
            if (!vnPayService.verifyPaymentSignature(params)) {
                log.error("Invalid signature for payment");
                return new ResponseMessage<>(false, "Chữ ký không hợp lệ", null);
            }

            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionNo = params.get("vnp_TransactionNo");
            String vnpBankCode = params.get("vnp_BankCode");

            // Find payment
            Payment payment = paymentRepository.findById(vnpTxnRef).orElse(null);
            if (payment == null) {
                log.error("Payment not found: {}", vnpTxnRef);
                return new ResponseMessage<>(false, "Không tìm thấy giao dịch", null);
            }

            // Check if already processed
            if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                log.warn("Payment {} already processed", vnpTxnRef);
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Giao dịch đã được xử lý");
                result.put("status", "success");
                return new ResponseMessage<>(true, "Giao dịch đã được xử lý", result);
            }

            // Update payment
            payment.setVnpResponseCode(vnpResponseCode);
            payment.setVnpTransactionNo(vnpTransactionNo);
            payment.setVnpBankCode(vnpBankCode);

            Map<String, Object> result = new HashMap<>();

            if ("00".equals(vnpResponseCode)) {
                // Payment success
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Process enrollment
                Cart cart = cartRepository.findById(payment.getCartId()).orElse(null);
                if (cart != null) {
                    // Enroll courses - Create UserProgress for each course
                    cart.getItems().forEach(item -> {
                        try {
                            // Check if already enrolled
                            if (userProgressRepository.findByUserIdAndCourseId(payment.getUserId(), item.getCourseId()).isEmpty()) {
                                // Create new UserProgress
                                UserProgress progress = UserProgress.builder()
                                        .userId(payment.getUserId())
                                        .courseId(item.getCourseId())
                                        .enrolledAt(LocalDateTime.now())
                                        .lastAccessedAt(LocalDateTime.now())
                                        .completedLessons(new ArrayList<>())
                                        .lessonsProgress(new ArrayList<>())
                                        .totalProgress(0)
                                        .build();

                                userProgressRepository.save(progress);
                                log.info("Enrolled user {} in course {}", payment.getUserId(), item.getCourseId());
                            }
                        } catch (Exception e) {
                            log.error("Error enrolling course {}: {}", item.getCourseId(), e.getMessage());
                        }
                    });

                    // Clear cart
                    cartRepository.delete(cart);
                    log.info("Cleared cart for user {}", payment.getUserId());
                }

                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
                result.put("paymentId", payment.getId());
                result.put("amount", payment.getAmount());

                return new ResponseMessage<>(true, "Thanh toán thành công", result);

            } else {
                // Payment failed
                if ("24".equals(vnpResponseCode)) {
                    payment.setStatus(Payment.PaymentStatus.CANCELLED);
                } else {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                }
                paymentRepository.save(payment);

                String message = vnPayService.getResponseMessage(vnpResponseCode);
                result.put("status", "failed");
                result.put("message", message);
                result.put("responseCode", vnpResponseCode);

                return new ResponseMessage<>(false, message, result);
            }

        } catch (Exception e) {
            log.error("Error processing payment return: {}", e.getMessage());
            return new ResponseMessage<>(false, "Lỗi xử lý thanh toán: " + e.getMessage(), null);
        }
    }

    /**
     * Get payment status
     */
    public ResponseMessage<Payment> getPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return new ResponseMessage<>(false, "Không tìm thấy giao dịch", null);
        }
        return new ResponseMessage<>(true, "Success", payment);
    }
}


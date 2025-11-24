package com.example.cake.payment.controller;

import com.example.cake.auth.model.User;
import com.example.cake.auth.repository.UserRepository;
import com.example.cake.payment.model.Payment;
import com.example.cake.payment.service.PaymentService;
import com.example.cake.response.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment Controller - VNPAY Integration
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * Create VNPAY payment - Direct course purchase
     *
     * @param request Payment request with courseIds
     * @param httpRequest HTTP request to get IP
     * @param authentication User authentication
     * @return Payment URL
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<ResponseMessage<Map<String, String>>> createPayment(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        // Get email from authentication (JWT subject)
        String email = authentication != null ? authentication.getName() : null;

        if (email == null) {
            log.error("User not authenticated");
            return ResponseEntity.ok(new ResponseMessage<>(false, "Vui lòng đăng nhập", null));
        }

        // Find user by email to get userId
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("User not found with email: {}", email);
            return ResponseEntity.ok(new ResponseMessage<>(false, "Không tìm thấy người dùng", null));
        }

        String userId = user.getId();

        // Get courseIds from request
        @SuppressWarnings("unchecked")
        java.util.List<String> courseIds = (java.util.List<String>) request.get("courseIds");

        if (courseIds == null || courseIds.isEmpty()) {
            return ResponseEntity.ok(new ResponseMessage<>(false, "Vui lòng chọn ít nhất một khóa học", null));
        }

        String orderInfo = (String) request.getOrDefault("orderInfo", "Thanh toan khoa hoc");
        String ipAddress = getClientIp(httpRequest);

        log.info("Create payment request from user: {} (email: {}), {} courses, IP: {}",
                userId, email, courseIds.size(), ipAddress);

        ResponseMessage<Map<String, String>> response = paymentService.createVNPayPayment(
                userId,
                courseIds,
                orderInfo,
                ipAddress
        );

        return ResponseEntity.ok(response);
    }

    /**
     * VNPAY Return URL - User redirect callback
     * Called when VNPAY redirects user back (GET request with query params)
     * Redirects to Frontend with payment result
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> vnpayReturn(
            @RequestParam Map<String, String> params
    ) {
        log.info("VNPAY return callback received with {} params", params.size());
        log.info("Params: {}", params);

        ResponseMessage<Map<String, Object>> response = paymentService.processVNPayReturn(params);
        return ResponseEntity.ok(response);
    }

    /**
     * VNPAY IPN URL - Server-to-server callback
     * Called by VNPAY server to confirm payment (GET request)
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIPN(
            @RequestParam Map<String, String> params
    ) {
        log.info("VNPAY IPN callback received with {} params", params.size());
        log.info("IPN Params: {}", params);

        ResponseMessage<Map<String, Object>> result = paymentService.processVNPayReturn(params);

        Map<String, String> response = new HashMap<>();
        if (result.isSuccess()) {
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment status
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<ResponseMessage<Payment>> getPaymentStatus(
            @PathVariable String paymentId,
            Authentication authentication
    ) {
        ResponseMessage<Payment> response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments for current user
     */
    @GetMapping("/my-payments")
    public ResponseEntity<ResponseMessage<java.util.List<Payment>>> getMyPayments(
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null) {
            return ResponseEntity.ok(new ResponseMessage<>(false, "Vui lòng đăng nhập", null));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(new ResponseMessage<>(false, "Không tìm thấy người dùng", null));
        }

        ResponseMessage<java.util.List<Payment>> response = paymentService.getUserPayments(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get successful payments for current user
     */
    @GetMapping("/my-payments/success")
    public ResponseEntity<ResponseMessage<java.util.List<Payment>>> getMySuccessfulPayments(
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null) {
            return ResponseEntity.ok(new ResponseMessage<>(false, "Vui lòng đăng nhập", null));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(new ResponseMessage<>(false, "Không tìm thấy người dùng", null));
        }

        ResponseMessage<java.util.List<Payment>> response = paymentService.getUserSuccessfulPayments(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        // Convert IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        return ipAddress;
    }
}


package com.example.cake.payment.service;

import com.example.cake.payment.config.VNPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPAY Service - Handle payment URL creation and signature verification
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    /**
     * Create VNPAY payment URL
     */
    public String createPaymentUrl(String paymentId, long amount, String orderInfo, String ipAddress) {
        try {
            // Validate inputs
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
            if (paymentId == null || paymentId.isEmpty()) {
                throw new IllegalArgumentException("PaymentId cannot be empty");
            }

            // Tạo params map
            Map<String, String> vnpParams = new TreeMap<>(); // TreeMap tự động sort theo key

            // Calculate amount in xu (VNĐ x 100) - đảm bảo là long
            long amountInXu = amount * 100;

            // Thêm TẤT CẢ params BẮT BUỘC theo tài liệu VNPAY
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", Long.toString(amountInXu));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", paymentId);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other"); // BẮT BUỘC - loại đơn hàng
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);

            // Create date
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Build query string (để tạo hash data và URL)
            StringBuilder query = new StringBuilder();

            boolean first = true;
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (!first) {
                        query.append('&');
                    }
                    first = false;

                    // Query string: URL encoded
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                         .append('=')
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                }
            }

            // Hash data = query string (CẢ HAI GIỐNG NHAU - URL ENCODED!)
            String hashData = query.toString();

            // Tính hash từ query string
            String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            String paymentUrl = vnPayConfig.getUrl() + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;

            // Debug logs
            log.info("========== VNPAY PAYMENT URL DEBUG ==========");
            log.info("Payment ID: {}", paymentId);
            log.info("Amount: {} VND", amount);
            log.info("TMN Code: {}", vnPayConfig.getTmnCode());
            log.info("Hash Secret: {}...", vnPayConfig.getHashSecret().substring(0, 15));
            log.info("Return URL: {}", vnPayConfig.getReturnUrl());
            log.info("Hash Data (URL-encoded): {}", hashData);
            log.info("Secure Hash: {}", vnpSecureHash);
            log.info("Final URL: {}", paymentUrl);
            log.info("==============================================");

            return paymentUrl;

        } catch (Exception e) {
            log.error("Error creating payment URL: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot create payment URL", e);
        }
    }

    /**
     * Verify VNPAY signature
     */
    public boolean verifyPaymentSignature(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        // Build hash data - PHẢI URL-ENCODED giống như khi tạo payment!
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!first) {
                    query.append('&');
                }
                first = false;

                try {
                    // URL encode giống như khi tạo payment
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding: {}", e.getMessage());
                }
            }
        }

        // Hash data = query string (URL-encoded)
        String hashData = query.toString();
        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        boolean isValid = calculatedHash.equals(vnpSecureHash);

        log.info("Verify signature: {}", isValid ? "SUCCESS" : "FAILED");
        if (!isValid) {
            log.error("Hash mismatch - Expected: {}, Got: {}", calculatedHash, vnpSecureHash);
            log.error("Hash data (URL-encoded): {}", hashData);
        }

        return isValid;
    }

    /**
     * Get VNPAY response message
     */
    public String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12": return "Thẻ/Tài khoản bị khóa";
            case "13": return "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch";
            case "24": return "Khách hàng hủy giao dịch";
            case "51": return "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65": return "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch vượt quá hạn mức thanh toán";
            default: return "Giao dịch thất bại";
        }
    }

    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error creating HMAC SHA512: {}", e.getMessage());
            return "";
        }
    }
}


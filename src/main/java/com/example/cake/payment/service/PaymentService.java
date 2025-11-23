package com.example.cake.payment.service;

import com.example.cake.course.model.Course;
import com.example.cake.course.repository.CourseRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Service - Handle payment processing
 * Updated: Removed cart/order dependencies, works directly with courses
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final VNPayService vnPayService;
    private final UserProgressRepository userProgressRepository;

    /**
     * Create VNPAY payment - Direct course purchase
     */
    public ResponseMessage<Map<String, String>> createVNPayPayment(
            String userId,
            List<String> courseIds,
            String orderInfo,
            String ipAddress
    ) {
        try {
            // Validate course IDs
            if (courseIds == null || courseIds.isEmpty()) {
                return new ResponseMessage<>(false, "Vui lòng chọn ít nhất một khóa học", null);
            }

            // Get courses from database
            List<Course> courses = new ArrayList<>();
            for (String courseId : courseIds) {
                Course course = courseRepository.findById(courseId).orElse(null);
                if (course == null) {
                    return new ResponseMessage<>(false, "Không tìm thấy khóa học: " + courseId, null);
                }
                if (course.getIsPublished() == null || !course.getIsPublished()) {
                    return new ResponseMessage<>(false, "Khóa học chưa được công khai: " + course.getTitle(), null);
                }

                // Check if user already enrolled
                if (userProgressRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
                    return new ResponseMessage<>(false, "Bạn đã đăng ký khóa học: " + course.getTitle(), null);
                }

                courses.add(course);
            }

            // Convert courses to payment items
            List<Payment.PaymentCourseItem> paymentCourses = courses.stream()
                    .map(course -> Payment.PaymentCourseItem.builder()
                            .courseId(course.getId())
                            .title(course.getTitle())
                            .thumbnailUrl(course.getThumbnailUrl())
                            .price(course.getPrice())
                            .discountedPrice(course.getDiscountedPrice())
                            .discountPercent(course.getDiscountPercent())
                            .instructorName(course.getInstructorName())
                            .level(course.getLevel())
                            .build())
                    .collect(Collectors.toList());

            // Calculate total price
            double totalPriceDouble = paymentCourses.stream()
                    .mapToDouble(item -> {
                        Double price = item.getDiscountedPrice() != null
                            ? item.getDiscountedPrice()
                            : item.getPrice();
                        return price != null ? price : 0.0;
                    })
                    .sum();

            long totalPrice = Math.round(totalPriceDouble);

            if (totalPrice <= 0) {
                return new ResponseMessage<>(false, "Tổng tiền không hợp lệ", null);
            }

            // Create payment record
            Payment payment = Payment.builder()
                    .userId(userId)
                    .courses(paymentCourses)
                    .amount(totalPrice)
                    .vnpOrderInfo(orderInfo)
                    .status(Payment.PaymentStatus.PENDING)
                    .paymentMethod(Payment.PaymentMethod.VNPAY)
                    .ipAddress(ipAddress)
                    .locale("vn")
                    .createdAt(LocalDateTime.now())
                    .build();

            payment = paymentRepository.save(payment);
            payment.setVnpTxnRef(payment.getId());
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

            log.info("Created payment {} for user {}, {} courses, amount: {} VND",
                    payment.getId(), userId, courses.size(), payment.getAmount());

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

                // Process enrollment - Enroll courses directly from payment
                if (payment.getCourses() != null && !payment.getCourses().isEmpty()) {
                    payment.getCourses().forEach(courseItem -> {
                        try {
                            // Check if already enrolled
                            if (userProgressRepository.findByUserIdAndCourseId(payment.getUserId(), courseItem.getCourseId()).isEmpty()) {
                                // Create new UserProgress
                                UserProgress progress = UserProgress.builder()
                                        .userId(payment.getUserId())
                                        .courseId(courseItem.getCourseId())
                                        .enrolledAt(LocalDateTime.now())
                                        .lastAccessedAt(LocalDateTime.now())
                                        .completedLessons(new ArrayList<>())
                                        .lessonsProgress(new ArrayList<>())
                                        .totalProgress(0)
                                        .build();

                                userProgressRepository.save(progress);
                                log.info("Enrolled user {} in course {} ({})",
                                        payment.getUserId(), courseItem.getCourseId(), courseItem.getTitle());
                            }
                        } catch (Exception e) {
                            log.error("Error enrolling course {}: {}", courseItem.getCourseId(), e.getMessage());
                        }
                    });
                }

                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
                result.put("paymentId", payment.getId());
                result.put("amount", payment.getAmount());
                result.put("coursesEnrolled", payment.getCourses().size());

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

    /**
     * Get all payments for a user
     */
    public ResponseMessage<List<Payment>> getUserPayments(String userId) {
        try {
            List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return new ResponseMessage<>(true, "Lấy lịch sử thanh toán thành công", payments);
        } catch (Exception e) {
            log.error("Error getting user payments: {}", e.getMessage());
            return new ResponseMessage<>(false, "Lỗi lấy lịch sử thanh toán", null);
        }
    }

    /**
     * Get successful payments for a user
     */
    public ResponseMessage<List<Payment>> getUserSuccessfulPayments(String userId) {
        try {
            List<Payment> payments = paymentRepository.findByUserIdAndStatus(userId, Payment.PaymentStatus.SUCCESS);
            return new ResponseMessage<>(true, "Lấy lịch sử thanh toán thành công", payments);
        } catch (Exception e) {
            log.error("Error getting user successful payments: {}", e.getMessage());
            return new ResponseMessage<>(false, "Lỗi lấy lịch sử thanh toán", null);
        }
    }
}


# 📊 ĐÁNH GIÁ CHỨC NĂNG THANH TOÁN VÀ VẬN CHUYỂN

**Ngày đánh giá:** 18/11/2025  
**Chức năng:** Thanh toán và Vận chuyển  
**Điểm ước tính:** **0.3 - 0.4 / 1.5** (20-27%)

---

## 🎯 TỔNG QUAN

Hệ thống **CHỈ có cấu trúc dữ liệu cơ bản** (PaymentMethod enum, OrderStatus enum) nhưng **CHƯA có bất kỳ logic xử lý thanh toán hay vận chuyển nào**. Đây là một trong những **điểm yếu nghiêm trọng nhất** của dự án.

---

## ✅ CÁC CHỨC NĂNG ĐÃ CÓ

### **PHẦN 1: THANH TOÁN (PAYMENT)**

#### **1. PaymentMethod Enum** ✅ **50%**

**File:** `PaymentMethod.java`

```java
public enum PaymentMethod {
    COD,                // Thanh toán khi nhận hàng
    BANK_TRANSFER,      // Chuyển khoản
    CREDIT_CARD,        // Thẻ tín dụng/ghi nợ
    E_WALLET,           // Ví điện tử
    ONLINE_PAYMENT      // Thanh toán trực tuyến (PayPal, Stripe, v.v.)
}
```

**Trạng thái:**
- ✅ Đã định nghĩa các phương thức thanh toán
- ✅ Phù hợp với hệ thống bán khóa học
- ❌ **KHÔNG có logic xử lý** cho từng phương thức
- ❌ **KHÔNG có integration** với payment gateway

**Vấn đề:**
- ⚠️ `COD` (Cash on Delivery) **KHÔNG phù hợp** với khóa học online
- ⚠️ Khóa học là digital product → Không cần "nhận hàng"
- ⚠️ Nên đổi `COD` thành `OFFLINE` hoặc `CASH`

**Nên sửa thành:**
```java
public enum PaymentMethod {
    BANK_TRANSFER,      // Chuyển khoản ngân hàng
    VNPAY,              // VNPay
    MOMO,               // MoMo Wallet
    ZALOPAY,            // ZaloPay
    CREDIT_CARD,        // Thẻ tín dụng/ghi nợ (qua gateway)
    PAYPAL,             // PayPal (thanh toán quốc tế)
    FREE                // Miễn phí (khóa học free hoặc đã có voucher 100%)
}
```

---

#### **2. PaymentMethod field trong Order** ✅ **100%**

**File:** `Order.java`

```java
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private List<OrderItem> items;
    private Double totalPrice;
    private Double discount;
    private String shippingAddress;      // ⚠️ Không phù hợp với khóa học online
    private PaymentMethod paymentMethod; // ✅ Có field này
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Điểm mạnh:**
- ✅ Có lưu paymentMethod khi tạo order
- ✅ Required field (validate trong createOrder)

**Vấn đề:**
- ❌ **KHÔNG có bảng Payment riêng** để lưu chi tiết thanh toán
- ❌ **KHÔNG lưu** payment transaction ID
- ❌ **KHÔNG lưu** payment status (pending/success/failed)
- ❌ **KHÔNG lưu** payment timestamp
- ❌ **KHÔNG lưu** payment amount (có thể khác totalPrice nếu có phí)

**Cần thêm Payment Model:**
```java
@Document(collection = "payments")
@Data
@Builder
public class Payment {
    @Id
    private String id;
    private String orderId;
    private String userId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;           // PENDING, SUCCESS, FAILED, REFUNDED
    private String transactionId;           // ID từ payment gateway
    private String gatewayResponse;         // Response từ VNPay, MoMo...
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String failureReason;
}

public enum PaymentStatus {
    PENDING,        // Chờ thanh toán
    PROCESSING,     // Đang xử lý
    SUCCESS,        // Thành công
    FAILED,         // Thất bại
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
```

---

#### **3. Validation PaymentMethod khi tạo Order** ✅ **100%**

**File:** `OrderService.java`

```java
public ResponseMessage<Order> createOrder(Order orderRequest) {
    // Validate payment method
    if (orderRequest.getPaymentMethod() == null) {
        return new ResponseMessage<>(false, "Payment method is required", null);
    }
    
    // ...
}
```

**Điểm mạnh:**
- ✅ Validate không null
- ✅ Required field

**Vấn đề:**
- ❌ **KHÔNG validate** PaymentMethod có hợp lệ không
- ❌ **KHÔNG xử lý** logic thanh toán tương ứng
- ❌ Sau khi tạo order, **KHÔNG redirect** đến payment gateway

---

#### **4. Lọc Order theo PaymentMethod** ✅ **100%**

**Repository:**
```java
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByPaymentMethod(PaymentMethod paymentMethod);
}
```

**Service:**
```java
public ResponseMessage<List<Order>> getOrdersByPaymentMethod(PaymentMethod paymentMethod) {
    if (paymentMethod == null) {
        return new ResponseMessage<>(false, "Payment method is required", null);
    }
    
    try {
        List<Order> orders = orderRepository.findAllByPaymentMethod(paymentMethod);
        return new ResponseMessage<>(true, "Orders retrieved successfully", orders);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to retrieve orders", null);
    }
}
```

**Điểm mạnh:**
- ✅ Có thể query orders theo payment method
- ✅ Hữu ích cho báo cáo, thống kê

**Vấn đề:**
- ⚠️ Method này **CHƯA được expose** qua Controller
- ⚠️ Chỉ có ở Service, chưa có API endpoint

---

### **PHẦN 2: VẬN CHUYỂN (SHIPPING)**

#### **1. ShippingAddress field** ⚠️ **30%**

**File:** `Order.java`

```java
private String shippingAddress;  // Địa chỉ giao hàng (email hoặc địa chỉ liên hệ)
```

**Hiện trạng:**
- ✅ Có field lưu địa chỉ
- ✅ Required field (validate khi tạo order)
- ⚠️ Kiểu `String` → Không structured
- ⚠️ Comment nói "email hoặc địa chỉ liên hệ" → Không rõ ràng

**Vấn đề với Khóa học Online:**
- ❌ Khóa học online **KHÔNG cần vận chuyển vật lý**
- ❌ "Shipping address" là **khái niệm sai**
- ❌ Nên đổi thành `contactEmail` hoặc `billingInfo`

**Cần sửa:**
```java
// Thay vì shippingAddress
private String contactEmail;        // Email nhận thông tin khóa học
private BillingInfo billingInfo;    // Thông tin hóa đơn (nếu cần)

@Data
public class BillingInfo {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;     // Địa chỉ xuất hóa đơn (optional)
    private String taxCode;     // Mã số thuế (nếu công ty)
}
```

---

#### **2. OrderStatus liên quan đến vận chuyển** ⚠️ **40%**

**File:** `OrderStatus.java`

```java
public enum OrderStatus {
    UNCONFIRMED,    // Chưa xác nhận
    PENDING,        // Chờ thanh toán
    CONFIRMED,      // Đã xác nhận
    PAID,           // Đã thanh toán (khóa học online không cần shipping) ✅ Đúng
    ENROLLED,       // Đã kích hoạt khóa học
    COMPLETED,      // Hoàn thành khóa học
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
```

**Điểm mạnh:**
- ✅ Có comment "khóa học online không cần shipping"
- ✅ Flow phù hợp với digital product:
  ```
  UNCONFIRMED → PENDING → PAID → ENROLLED → COMPLETED
  ```
- ✅ Có REFUNDED status

**Vấn đề:**
- ❌ **KHÔNG có status** PROCESSING (đang xử lý thanh toán)
- ❌ **KHÔNG có status** PAYMENT_FAILED
- ⚠️ CONFIRMED và PAID khác nhau như thế nào? Không rõ ràng

**Nên sửa thành:**
```java
public enum OrderStatus {
    // Payment Flow
    PENDING,            // Chờ thanh toán
    PROCESSING,         // Đang xử lý thanh toán
    PAYMENT_FAILED,     // Thanh toán thất bại
    PAID,               // Đã thanh toán thành công
    
    // Enrollment Flow
    ENROLLING,          // Đang kích hoạt khóa học
    ENROLLED,           // Đã kích hoạt, học viên có thể học
    
    // Completion & Cancellation
    IN_PROGRESS,        // Đang học
    COMPLETED,          // Hoàn thành khóa học
    CANCELLED,          // Đã hủy (trước khi thanh toán)
    REFUNDED            // Đã hoàn tiền
}
```

**Flow mới:**
```
1. Tạo order → PENDING
2. User chọn payment method → redirect payment gateway
3. Đang xử lý → PROCESSING
4a. Thành công → PAID → ENROLLING → ENROLLED → IN_PROGRESS → COMPLETED
4b. Thất bại → PAYMENT_FAILED
5. Hoặc user hủy → CANCELLED
6. Hoặc admin refund → REFUNDED
```

---

## ❌ CÁC CHỨC NĂNG THIẾU HOÀN TOÀN

### **1. Tích hợp Payment Gateway** ❌ **0%** - **NGHIÊM TRỌNG**

**Yêu cầu:**
Tích hợp ít nhất 1 payment gateway phổ biến tại Việt Nam:
- VNPay
- MoMo
- ZaloPay
- PayOS

**Hiện trạng:**
- ❌ **KHÔNG có** bất kỳ payment gateway nào
- ❌ **KHÔNG có** VNPayService
- ❌ **KHÔNG có** MoMoService
- ❌ **KHÔNG có** payment callback URL
- ❌ **KHÔNG có** payment verification

**Cần bổ sung:**

#### **A. Tích hợp VNPay** (Phổ biến nhất tại VN)

**Thư viện cần thêm vào `pom.xml`:**
```xml
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.15</version>
</dependency>
```

**VNPayConfig.java:**
```java
@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    private String tmnCode;         // Mã website
    private String hashSecret;      // Secret key
    private String url;             // https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
    private String returnUrl;       // http://localhost:8080/api/payment/vnpay/callback
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
}
```

**application.yml:**
```yaml
vnpay:
  tmn-code: YOUR_TMN_CODE
  hash-secret: YOUR_HASH_SECRET
  url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  return-url: http://localhost:8080/api/payment/vnpay/callback
```

**VNPayService.java:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {
    
    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    
    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(String orderId, HttpServletRequest request) 
        throws UnsupportedEncodingException {
        
        // 1. Lấy order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 2. Tạo Payment record
        Payment payment = Payment.builder()
            .orderId(orderId)
            .userId(order.getUserId())
            .amount(order.getTotalPrice())
            .method(PaymentMethod.VNPAY)
            .status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
        Payment savedPayment = paymentRepository.save(payment);
        
        // 3. Build VNPay parameters
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf((long)(order.getTotalPrice() * 100))); // VNPay yêu cầu * 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", savedPayment.getId()); // Dùng paymentId làm reference
        vnp_Params.put("vnp_OrderInfo", "Thanh toan khoa hoc - Order: " + orderId);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", getIpAddress(request));
        
        // Format: yyyyMMddHHmmss
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss")
            .format(new Date());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        // Expire time: 15 phút
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = new SimpleDateFormat("yyyyMMddHHmmss")
            .format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // 4. Sort parameters và build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        // 5. Generate secure hash
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        // 6. Return payment URL
        String paymentUrl = vnPayConfig.getUrl() + "?" + query.toString();
        
        log.info("VNPay payment URL created for order: {}", orderId);
        return paymentUrl;
    }
    
    /**
     * Xử lý callback từ VNPay
     */
    public ResponseMessage<PaymentCallbackResponse> handleCallback(
        Map<String, String> vnpParams) {
        
        try {
            // 1. Verify signature
            String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHashType");
            
            String signValue = hashAllFields(vnpParams);
            
            if (!signValue.equals(vnp_SecureHash)) {
                log.error("Invalid VNPay signature");
                return new ResponseMessage<>(false, "Invalid signature", null);
            }
            
            // 2. Get payment info
            String paymentId = vnpParams.get("vnp_TxnRef");
            String responseCode = vnpParams.get("vnp_ResponseCode");
            String transactionNo = vnpParams.get("vnp_TransactionNo");
            String bankCode = vnpParams.get("vnp_BankCode");
            String amount = vnpParams.get("vnp_Amount");
            
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // 3. Check response code
            if ("00".equals(responseCode)) {
                // Thanh toán thành công
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setTransactionId(transactionNo);
                payment.setPaidAt(LocalDateTime.now());
                payment.setGatewayResponse(vnpParams.toString());
                paymentRepository.save(payment);
                
                // Update order status
                order.setStatus(OrderStatus.PAID);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                // TODO: Kích hoạt khóa học cho user
                // enrollmentService.enrollCourses(order);
                
                PaymentCallbackResponse response = PaymentCallbackResponse.builder()
                    .success(true)
                    .orderId(order.getId())
                    .paymentId(payment.getId())
                    .transactionId(transactionNo)
                    .amount(Double.parseDouble(amount) / 100)
                    .message("Thanh toán thành công")
                    .build();
                
                return new ResponseMessage<>(true, "Payment successful", response);
                
            } else {
                // Thanh toán thất bại
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(getVNPayErrorMessage(responseCode));
                payment.setGatewayResponse(vnpParams.toString());
                paymentRepository.save(payment);
                
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                return new ResponseMessage<>(false, 
                    "Payment failed: " + getVNPayErrorMessage(responseCode), null);
            }
            
        } catch (Exception e) {
            log.error("Error handling VNPay callback", e);
            return new ResponseMessage<>(false, "Error processing payment", null);
        }
    }
    
    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Hash all fields
     */
    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        return hmacSHA512(vnPayConfig.getHashSecret(), sb.toString());
    }
    
    /**
     * Get IP Address
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * Get VNPay error message
     */
    private String getVNPayErrorMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán.";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng thanh toán đang bảo trì.";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default: return "Giao dịch thất bại";
        }
    }
}
```

**PaymentController.java:**
```java
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final VNPayService vnPayService;
    
    /**
     * Tạo payment URL
     */
    @PostMapping("/vnpay/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseMessage<String>> createVNPayPayment(
        @RequestBody CreatePaymentRequest request,
        HttpServletRequest httpRequest) {
        
        try {
            String paymentUrl = vnPayService.createPaymentUrl(
                request.getOrderId(), 
                httpRequest
            );
            
            return ResponseEntity.ok(
                new ResponseMessage<>(true, "Payment URL created", paymentUrl)
            );
        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            return ResponseEntity.badRequest().body(
                new ResponseMessage<>(false, e.getMessage(), null)
            );
        }
    }
    
    /**
     * VNPay callback
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> vnpayCallback(
        @RequestParam Map<String, String> allParams) {
        
        log.info("VNPay callback received: {}", allParams);
        
        ResponseMessage<PaymentCallbackResponse> result = 
            vnPayService.handleCallback(allParams);
        
        if (result.isSuccess()) {
            // Redirect về trang success
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/payment/success?orderId=" 
                    + result.getData().getOrderId()))
                .build();
        } else {
            // Redirect về trang failed
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/payment/failed?message=" 
                    + URLEncoder.encode(result.getMessage(), StandardCharsets.UTF_8)))
                .build();
        }
    }
    
    /**
     * Check payment status
     */
    @GetMapping("/status/{paymentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseMessage<Payment>> getPaymentStatus(
        @PathVariable String paymentId) {
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        return ResponseEntity.ok(
            new ResponseMessage<>(true, "Payment status", payment)
        );
    }
}
```

**DTOs:**
```java
@Data
public class CreatePaymentRequest {
    private String orderId;
}

@Data
@Builder
public class PaymentCallbackResponse {
    private boolean success;
    private String orderId;
    private String paymentId;
    private String transactionId;
    private Double amount;
    private String message;
}
```

**Flow hoàn chỉnh:**
```
1. User tạo order → Order status = PENDING
2. Frontend call POST /api/payment/vnpay/create
   → Backend tạo Payment record (status = PENDING)
   → Trả về VNPay payment URL
3. Frontend redirect user đến VNPay payment URL
4. User thanh toán trên VNPay
5. VNPay callback về GET /api/payment/vnpay/callback
   → Backend verify signature
   → Update Payment status (SUCCESS/FAILED)
   → Update Order status (PAID/PAYMENT_FAILED)
   → Redirect về frontend success/failed page
6. Frontend hiển thị kết quả
```

---

#### **B. Tích hợp MoMo** (Alternative)

**MoMoConfig.java:**
```java
@Configuration
@ConfigurationProperties(prefix = "momo")
@Data
public class MoMoConfig {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String endpoint;        // https://test-payment.momo.vn/v2/gateway/api/create
    private String redirectUrl;
    private String ipnUrl;          // IPN = Instant Payment Notification
}
```

**MoMoService.java:**
```java
@Service
@RequiredArgsConstructor
public class MoMoService {
    
    private final MoMoConfig moMoConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    
    public String createPaymentUrl(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Create payment
        Payment payment = Payment.builder()
            .orderId(orderId)
            .userId(order.getUserId())
            .amount(order.getTotalPrice())
            .method(PaymentMethod.MOMO)
            .status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
        Payment savedPayment = paymentRepository.save(payment);
        
        // Build MoMo request
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan khoa hoc - Order: " + orderId;
        long amount = order.getTotalPrice().longValue();
        
        // Generate signature
        String rawSignature = String.format(
            "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
            moMoConfig.getAccessKey(),
            amount,
            "",  // extraData
            moMoConfig.getIpnUrl(),
            savedPayment.getId(),
            orderInfo,
            moMoConfig.getPartnerCode(),
            moMoConfig.getRedirectUrl(),
            requestId,
            "captureWallet"
        );
        
        String signature = hmacSHA256(moMoConfig.getSecretKey(), rawSignature);
        
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", moMoConfig.getPartnerCode());
        requestBody.put("accessKey", moMoConfig.getAccessKey());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", savedPayment.getId());
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", moMoConfig.getRedirectUrl());
        requestBody.put("ipnUrl", moMoConfig.getIpnUrl());
        requestBody.put("extraData", "");
        requestBody.put("requestType", "captureWallet");
        requestBody.put("signature", signature);
        requestBody.put("lang", "vi");
        
        // Call MoMo API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            moMoConfig.getEndpoint(),
            entity,
            Map.class
        );
        
        Map<String, Object> responseBody = response.getBody();
        
        if (responseBody != null && "0".equals(String.valueOf(responseBody.get("resultCode")))) {
            return (String) responseBody.get("payUrl");
        } else {
            throw new RuntimeException("Failed to create MoMo payment");
        }
    }
    
    private String hmacSHA256(String key, String data) throws Exception {
        Mac hmac256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        hmac256.init(secretKey);
        byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
```

---

### **2. Enrollment Service** ❌ **0%** - **QUAN TRỌNG**

**Yêu cầu:**
Sau khi thanh toán thành công, tự động kích hoạt khóa học cho user.

**Cần bổ sung:**

```java
// Enrollment.java
@Document(collection = "enrollments")
@Data
@Builder
public class Enrollment {
    @Id
    private String id;
    private String userId;
    private String courseId;
    private String orderId;
    private EnrollmentStatus status;    // ACTIVE, COMPLETED, EXPIRED
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;    // Nếu khóa học có thời hạn
    private Integer progress;           // % hoàn thành (0-100)
}

// EnrollmentService.java
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    /**
     * Kích hoạt khóa học sau khi thanh toán
     */
    public void enrollCoursesFromOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            // Kiểm tra đã enroll chưa
            Optional<Enrollment> existing = enrollmentRepository
                .findByUserIdAndCourseId(order.getUserId(), item.getCourseId());
            
            if (existing.isEmpty()) {
                Enrollment enrollment = Enrollment.builder()
                    .userId(order.getUserId())
                    .courseId(item.getCourseId())
                    .orderId(order.getId())
                    .status(EnrollmentStatus.ACTIVE)
                    .enrolledAt(LocalDateTime.now())
                    .progress(0)
                    .build();
                
                enrollmentRepository.save(enrollment);
                
                // TODO: Gửi email thông báo
                // emailService.sendEnrollmentEmail(user, course);
            }
        }
        
        // Update order status
        order.setStatus(OrderStatus.ENROLLED);
        orderRepository.save(order);
    }
    
    /**
     * Lấy danh sách khóa học user đã mua
     */
    public List<Enrollment> getUserEnrollments(String userId) {
        return enrollmentRepository.findByUserId(userId);
    }
    
    /**
     * Kiểm tra user có quyền access khóa học không
     */
    public boolean canAccessCourse(String userId, String courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository
            .findByUserIdAndCourseId(userId, courseId);
        
        return enrollment.isPresent() && 
               enrollment.get().getStatus() == EnrollmentStatus.ACTIVE;
    }
}
```

**Gọi trong VNPayService callback:**
```java
// Sau khi payment SUCCESS
if ("00".equals(responseCode)) {
    // ...update payment, order...
    
    // Kích hoạt khóa học
    enrollmentService.enrollCoursesFromOrder(order);
    
    // Gửi email
    emailService.sendPaymentSuccessEmail(order);
}
```

---

### **3. Refund/Hoàn tiền** ❌ **0%**

**Yêu cầu:**
- Admin có thể hoàn tiền cho user
- Gọi API refund của payment gateway
- Update payment và order status

**Cần bổ sung:**

```java
// RefundService.java
@Service
@RequiredArgsConstructor
public class RefundService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VNPayService vnPayService;
    
    /**
     * Hoàn tiền
     */
    public ResponseMessage<Refund> refundOrder(String orderId, String reason, String adminId) {
        // 1. Tìm order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != OrderStatus.PAID && 
            order.getStatus() != OrderStatus.ENROLLED) {
            return new ResponseMessage<>(false, "Order không thể hoàn tiền", null);
        }
        
        // 2. Tìm payment
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return new ResponseMessage<>(false, "Payment chưa thành công", null);
        }
        
        // 3. Gọi API refund của payment gateway
        boolean refundSuccess = false;
        
        switch (payment.getMethod()) {
            case VNPAY:
                refundSuccess = vnPayService.refund(payment);
                break;
            case MOMO:
                refundSuccess = moMoService.refund(payment);
                break;
            default:
                return new ResponseMessage<>(false, "Payment method không hỗ trợ refund", null);
        }
        
        if (refundSuccess) {
            // 4. Update payment
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            // 5. Update order
            order.setStatus(OrderStatus.REFUNDED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            // 6. Tạo refund record
            Refund refund = Refund.builder()
                .orderId(orderId)
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .reason(reason)
                .refundedBy(adminId)
                .refundedAt(LocalDateTime.now())
                .build();
            refundRepository.save(refund);
            
            // 7. Revoke enrollment
            enrollmentService.revokeEnrollment(orderId);
            
            return new ResponseMessage<>(true, "Hoàn tiền thành công", refund);
        }
        
        return new ResponseMessage<>(false, "Hoàn tiền thất bại", null);
    }
}
```

---

### **4. Payment Report/Analytics** ❌ **0%**

**Yêu cầu:**
- Thống kê doanh thu theo ngày/tháng
- Thống kê theo payment method
- Tỷ lệ thanh toán thành công/thất bại

**Cần bổ sung:**

```java
// PaymentAnalyticsService.java
@Service
@RequiredArgsConstructor
public class PaymentAnalyticsService {
    
    private final PaymentRepository paymentRepository;
    
    /**
     * Doanh thu theo ngày
     */
    public Map<String, Double> getRevenueByDate(LocalDate from, LocalDate to) {
        List<Payment> payments = paymentRepository
            .findByStatusAndPaidAtBetween(
                PaymentStatus.SUCCESS, 
                from.atStartOfDay(), 
                to.atTime(23, 59, 59)
            );
        
        return payments.stream()
            .collect(Collectors.groupingBy(
                p -> p.getPaidAt().toLocalDate().toString(),
                Collectors.summingDouble(Payment::getAmount)
            ));
    }
    
    /**
     * Thống kê theo payment method
     */
    public Map<PaymentMethod, Long> getCountByMethod() {
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);
        return payments.stream()
            .collect(Collectors.groupingBy(
                Payment::getMethod,
                Collectors.counting()
            ));
    }
    
    /**
     * Tỷ lệ thành công/thất bại
     */
    public Map<String, Long> getSuccessRate() {
        List<Payment> all = paymentRepository.findAll();
        return all.stream()
            .collect(Collectors.groupingBy(
                p -> p.getStatus() == PaymentStatus.SUCCESS ? "SUCCESS" : "FAILED",
                Collectors.counting()
            ));
    }
}
```

---

### **5. Invoice Generation** ❌ **0%**

**Yêu cầu:**
- Tạo hóa đơn PDF sau khi thanh toán
- Gửi hóa đơn qua email
- Download hóa đơn

**Code đã có trong file `DANH_GIA_GIO_HANG_MUA_HANG.md`**

---

## 📊 BẢNG TỔNG KẾT CHI TIẾT

### **THANH TOÁN**

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **PaymentMethod enum** | ✅ Có | 50% | **0.13** | Có enum nhưng không dùng |
| 2 | **Payment Model** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 3 | **Payment Gateway** | ❌ Không | 0% | **0.00** | ❌ THIẾU HOÀN TOÀN |
| 4 | **Payment Callback** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 5 | **Payment Verification** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 6 | **Refund** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 7 | **Payment Analytics** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 8 | **Invoice** | ❌ Không | 0% | **0.00** | ❌ THIẾU |

**Tổng Thanh toán:** **0.13 / 2.0** (6.5%)

---

### **VẬN CHUYỂN (KHÔNG ÁP DỤNG CHO KHÓA HỌC)**

| # | Chức năng | Trạng thái | Phù hợp? | Ghi chú |
|---|-----------|-----------|----------|---------|
| 1 | **ShippingAddress** | ⚠️ Có | ❌ Không | Khóa học online không cần shipping |
| 2 | **Enrollment** | ❌ Không | ✅ Cần | Thay thế cho shipping - kích hoạt khóa học |
| 3 | **Access Control** | ❌ Không | ✅ Cần | Kiểm tra quyền truy cập khóa học |

**Lưu ý:** Với khóa học online, **KHÔNG CẦN vận chuyển vật lý**. Thay vào đó cần:
- ✅ **Enrollment System** (kích hoạt khóa học)
- ✅ **Access Control** (kiểm tra quyền)
- ✅ **Course Delivery** (gửi link/tài liệu)

---

**TỔNG ĐIỂM:** **0.13 / 2.0**  
**Nếu tính theo thang 1.5 điểm:** **~0.1 / 1.5** (6.7%)

---

## 🎯 KHUYẾN NGHỊ ĐỂ ĐẠT ĐIỂM TỐI ĐA

### **PRIORITY 1 - BẮT BUỘC** (để đạt 0.8-1.0 điểm)

#### **1. Tích hợp VNPay** ⏱️ **4-6 giờ**
- Tạo Payment Model
- VNPayConfig, VNPayService
- PaymentController (create, callback)
- Test với VNPay sandbox

**Checklist:**
- [ ] Tạo `Payment` model
- [ ] Tạo `PaymentRepository`
- [ ] Tạo `VNPayConfig` với application.yml
- [ ] Tạo `VNPayService` với createPaymentUrl()
- [ ] Tạo `VNPayService` với handleCallback()
- [ ] Tạo `PaymentController`
- [ ] Test flow hoàn chỉnh

---

#### **2. Enrollment Service** ⏱️ **2-3 giờ**
- Tạo Enrollment Model
- EnrollmentService
- Tự động kích hoạt sau payment success

**Checklist:**
- [ ] Tạo `Enrollment` model
- [ ] Tạo `EnrollmentRepository`
- [ ] Tạo `EnrollmentService`
- [ ] Gọi enrollCoursesFromOrder() trong payment callback
- [ ] API lấy courses đã mua
- [ ] Middleware check access course

---

### **PRIORITY 2 - QUAN TRỌNG** (để đạt 1.2-1.3 điểm)

#### **3. Payment Status Tracking** ⏱️ **1 giờ**
- API check payment status
- Hiển thị payment history

#### **4. Refund System** ⏱️ **2 giờ**
- Admin refund API
- Call gateway refund API
- Update statuses

---

### **PRIORITY 3 - NÂNGCao** (để đạt 1.4-1.5 điểm)

#### **5. Multiple Payment Methods** ⏱️ **3-4 giờ**
- Tích hợp MoMo
- Tích hợp ZaloPay
- Factory pattern cho payment

#### **6. Invoice & Email** ⏱️ **2 giờ**
- Generate PDF invoice
- Send email sau payment
- Download invoice API

---

## 📈 DỰ ĐOÁN ĐIỂM SAU KHI CẢI THIỆN

| Scenario | Chức năng thêm | Điểm ước tính | % |
|----------|----------------|---------------|---|
| **Hiện tại** | - | **0.1 / 1.5** | 6.7% |
| **+ VNPay Integration** | Payment gateway | **0.8 / 1.5** | 53% |
| **+ Enrollment Service** | Kích hoạt khóa học | **1.0 / 1.5** | 67% |
| **+ Payment Tracking** | Theo dõi thanh toán | **1.1 / 1.5** | 73% |
| **+ Refund System** | Hoàn tiền | **1.25 / 1.5** | 83% |
| **+ Multi Payment + Invoice** | Đầy đủ | **1.5 / 1.5** | 100% |

---

## 📋 CHECKLIST HÀNH ĐỘNG

### **Để đạt 1.0 điểm (67%):**
- [ ] **Tích hợp VNPay** (4-6 giờ) - BẮT BUỘC
- [ ] **Enrollment Service** (2-3 giờ) - BẮT BUỘC

### **Để đạt 1.25 điểm (83%):**
- [ ] Payment Status API (1 giờ)
- [ ] Refund System (2 giờ)

### **Để đạt 1.5 điểm (100%):**
- [ ] Tích hợp MoMo/ZaloPay (3-4 giờ)
- [ ] Invoice Generation (2 giờ)
- [ ] Email Notifications (1 giờ)

---

## 🎯 KẾT LUẬN

### **Điểm mạnh:**
✅ Có cấu trúc dữ liệu cơ bản (enum)  
✅ Có validate payment method  
✅ OrderStatus phù hợp với khóa học online  

### **Điểm yếu NGHIÊM TRỌNG:**
❌ **KHÔNG có payment gateway nào** (VNPay, MoMo, ZaloPay)  
❌ **KHÔNG có Payment Model** để lưu chi tiết thanh toán  
❌ **KHÔNG có Enrollment Service** để kích hoạt khóa học  
❌ **KHÔNG có payment callback** xử lý  
❌ **KHÔNG có refund** chức năng  
❌ **KHÔNG có invoice** generation  
❌ Khái niệm "shipping" **KHÔNG phù hợp** với khóa học online  

### **Ưu tiên tuyệt đối:**
🔴 **Tích hợp VNPay NGAY** (4-6 giờ) - QUAN TRỌNG NHẤT  
🔴 **Enrollment Service** (2-3 giờ) - BẮT BUỘC  

**Không có 2 chức năng trên, hệ thống KHÔNG THỂ bán khóa học được!**

---

**Tổng kết:**  
Đây là **điểm yếu nghiêm trọng nhất** của dự án. Hệ thống **CHỈ có cấu trúc** nhưng **HOÀN TOÀN THIẾU implementation**. **BẮT BUỘC** phải tích hợp payment gateway và enrollment service để hệ thống có thể hoạt động.

**Điểm hiện tại chỉ 0.1/1.5 (6.7%) - NGUY HIỂM!**

---

**Ngày đánh giá:** 18/11/2025  
**Người đánh giá:** Backend Analysis Team  
**Phiên bản:** 1.0  
**Mức độ ưu tiên:** 🔴 **CRITICAL**


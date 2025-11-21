# 📊 ĐÁNH GIÁ CHỨC NĂNG ĐĂNG NHẬP & BẢO MẬT

**Ngày đánh giá:** 18/11/2025  
**Yêu cầu:** Chức năng đăng nhập & bảo mật (1.5 điểm)  
**Điểm ước tính:** **1.0 - 1.2 / 1.5** (67-80%)

---

## 🎯 TỔNG QUAN

Hệ thống đã triển khai **6/7 chức năng** cơ bản, với những điểm mạnh về JWT, OTP verification, và Google OAuth. Tuy nhiên, vẫn thiếu 2 chức năng quan trọng: **2FA khi đăng nhập** và **Logout API**.

---

## ✅ CÁC CHỨC NĂNG ĐÃ CÓ

### **1. Đăng ký tài khoản** ✅ **100%**

**Endpoints:**
- `POST /api/auth/register` - Đăng ký email/phone
- `POST /api/auth/verify-otp` - Xác thực OTP
- `POST /api/auth/google` - Đăng ký Google OAuth

**Tính năng:**
- ✅ Đăng ký bằng **email** (validation format)
- ✅ Đăng ký bằng **số điện thoại** (validation `0\d{9}`)
- ✅ Đăng ký bằng **Google** (Firebase Auth)
- ✅ Gửi **OTP 6 số** qua email
- ✅ OTP có **TTL 5 phút** trong Redis
- ✅ Mã hóa password bằng **BCrypt**

**Ví dụ:**
```json
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "123456",
  "fullname": "Nguyễn Văn A",
  "phoneNumber": "0901234567"
}
→ Response: { "token": "uuid-token" }

POST /api/auth/verify-otp
{
  "token": "uuid-token",
  "otp": "123456"
}
→ User được tạo trong database
```

---

### **2. Đăng nhập** ✅ **90%**

**Endpoints:**
- `POST /api/auth/login` - Email/Password
- `POST /api/auth/google` - Google OAuth
- `POST /api/auth/refresh-token` - Refresh token

**Tính năng:**
- ✅ Đăng nhập **email/mật khẩu**
- ✅ Đăng nhập **Google** (auto-create user)
- ✅ JWT **Access Token** (ngắn hạn)
- ✅ **Refresh Token** trong Cookie (HttpOnly, 7 ngày)
- ✅ Refresh Token lưu trong **Redis**
- ✅ Kiểm tra **account active/inactive**
- ✅ Password matching với **BCrypt**

**Flow login:**
```
1. User gửi email/password
2. Backend verify password
3. Tạo UserPrincipal
4. Generate Access Token (JWT)
5. Generate Refresh Token (UUID)
6. Lưu Refresh Token vào Redis (TTL 7 ngày)
7. Gửi Refresh Token qua HttpOnly Cookie
8. Trả về Access Token
```

**Thiếu:**
- ⚠️ Chưa có **rate limiting** (chống brute force)
- ⚠️ Chưa log **failed login attempts**
- ⚠️ Chưa có **OTP khi đăng nhập** (cho 2FA)

---

### **3. Quên mật khẩu / Đặt lại mật khẩu** ✅ **100%**

**Endpoints:**
- `POST /api/auth/forget-password` - Gửi OTP
- `POST /api/auth/verify-otpPassword` - Verify OTP
- `POST /api/auth/reset-password` - Reset password mới

**Flow hoàn chỉnh:**
```
Step 1: Forget Password
POST /api/auth/forget-password
{
  "email": "user@example.com"
}
→ Gửi OTP qua email, trả về token

Step 2: Verify OTP
POST /api/auth/verify-otpPassword
{
  "token": "uuid-from-step1",
  "otp": "123456"
}
→ Xác nhận OTP đúng

Step 3: Reset Password
POST /api/auth/reset-password
{
  "token": "uuid-from-step1",
  "newPassword": "newPassword123"
}
→ Update password mới, xóa token
```

**Tính năng:**
- ✅ OTP 6 số gửi qua email
- ✅ Token có **TTL 5 phút**
- ✅ Token bị xóa sau reset thành công
- ✅ Password được mã hóa BCrypt

---

### **4. Xác thực hai yếu tố (2FA)** ❌ **0%**

**Trạng thái:** **CHƯA CÓ**

**Hiện có:**
- ⚠️ OTP khi **đăng ký** (verify email)
- ⚠️ OTP khi **quên mật khẩu**
- ❌ KHÔNG có OTP khi **đăng nhập**

**Thiếu:**
- ❌ Không có QR code generation (TOTP)
- ❌ Không có Google Authenticator
- ❌ Không có backup codes
- ❌ Không có setting b��t/tắt 2FA
- ❌ Không có SMS OTP
- ❌ **Không có OTP khi đăng nhập** (quan trọng nhất!)

**Cần bổ sung:**
```java
// Flow 2FA khi login
1. User login thành công
2. Kiểm tra user.twoFactorEnabled == true
3. Gửi OTP qua email/SMS
4. User nhập OTP
5. Verify OTP
6. Trả về Access Token
```

---

### **5. Quản lý hồ sơ người dùng** ✅ **95%**

**Endpoints:**
- `GET /api/users/find-userId` - Xem thông tin
- `PUT /api/users/update-user` - Cập nhật thông tin
- `PUT /api/users/change-password` - Đổi mật khẩu

**Các trường quản lý:**
```java
- fullname         // Họ tên
- phoneNumber      // Số điện thoại
- gender           // Giới tính
- dateOfBirth      // Ngày sinh
- avatarUrl        // Ảnh đại diện
- address {        // Địa chỉ chi tiết
    street
    ward
    district
    city
  }
```

**Tính năng:**
- ✅ **Upload ảnh đại diện** (multipart/form-data)
- ✅ Validation image types (JPG, PNG, GIF, SVG, BMP, WebP...)
- ✅ Lưu file vào `uploads/avatars/` với **UUID filename**
- ✅ Cập nhật địa chỉ chi tiết (Address object)
- ✅ **Change password** có verify mật khẩu cũ
- ✅ Google account **không được đổi mật khẩu**
- ✅ Sử dụng `@AuthenticationPrincipal` để lấy user hiện tại

**Ví dụ Update Profile:**
```http
PUT /api/users/update-user
Content-Type: multipart/form-data

- request: {
    "fullname": "Nguyễn Văn B",
    "phoneNumber": "0987654321",
    "gender": "MALE",
    "dateOfBirth": "1990-01-01",
    "address": {
      "street": "123 ABC",
      "ward": "Phường 1",
      "district": "Quận 1",
      "city": "TP.HCM"
    }
  }
- avatarFile: [binary file]
```

**Thiếu nhỏ:**
- ⚠️ Chưa có **delete avatar**
- ⚠️ Chưa có **crop/resize ảnh**

---

### **6. Đăng xuất an toàn** ❌ **0%**

**Trạng thái:** **CHƯA CÓ ENDPOINT**

**Vấn đề:**
- ❌ Không tìm thấy endpoint `/logout`
- ❌ Frontend phải tự xóa token (không an toàn)
- ❌ Refresh token vẫn còn trong Redis/Cookie cho đến khi hết hạn
- ❌ Không có cách revoke token ngay lập tức

**Hiện có:**
- ✅ Refresh Token lưu trong Redis (có thể xóa)
- ✅ Refresh Token trong HttpOnly Cookie

**Cần bổ sung:**
```java
@PostMapping("/api/auth/logout")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<ResponseMessage<String>> logout(
    HttpServletRequest request,
    HttpServletResponse response,
    @AuthenticationPrincipal UserPrincipal userPrincipal) {
    
    // 1. Lấy refresh token từ cookie
    Cookie[] cookies = request.getCookies();
    String refreshToken = null;
    for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
            refreshToken = cookie.getValue();
            break;
        }
    }
    
    // 2. Xóa refresh token khỏi Redis
    if (refreshToken != null) {
        redisService.deleteRefreshToken(refreshToken);
    }
    
    // 3. Xóa cookie
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
    
    // 4. (Optional) Blacklist access token
    // blacklistService.addToken(accessToken, expirationTime);
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Đăng xuất thành công", null)
    );
}
```

---

## 📊 BẢNG TỔNG KẾT CHI TIẾT

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Đăng ký** (email/phone/social) | ✅ Có đầy đủ | 100% | **0.25** | Email validation, OTP, Google OAuth |
| 2 | **Đăng nhập** (email/password/social) | ✅ Có | 90% | **0.22** | Thiếu rate limiting, login history |
| 3 | **Quên/Reset mật khẩu** | ✅ Hoàn chỉnh | 100% | **0.25** | Flow 3 bước, OTP verification |
| 4 | **Xác thực 2FA** | ❌ Chưa có | 0% | **0.00** | ❌ THIẾU - chỉ có OTP đăng ký/reset |
| 5 | **Quản lý hồ sơ** | ✅ Có đầy đủ | 95% | **0.24** | Avatar upload, update info |
| 6 | **Đăng xuất an toàn** | ❌ Chưa có | 0% | **0.00** | ❌ THIẾU - không có endpoint |

**TỔNG ĐIỂM:** **0.96 / 1.5** ≈ **64%**

---

## 🔒 ĐÁNH GIÁ BẢO MẬT

### **✅ ĐIỂM MẠNH**

#### **1. JWT Implementation - Rất tốt**
```
✅ Access Token (ngắn hạn)
✅ Refresh Token (dài hạn, 7 ngày)
✅ Refresh Token lưu trong HttpOnly Cookie (chống XSS)
✅ Refresh Token lưu trong Redis (có thể revoke)
✅ JWT Filter cho authentication
```

#### **2. Password Security - Tốt**
```
✅ BCrypt hashing (cost factor mặc định)
✅ Minimum 6 characters
✅ Không log password trong code
✅ Verify password cũ khi đổi mật khẩu mới
```

#### **3. Email/OTP Verification - Tốt**
```
✅ OTP 6 số ngẫu nhiên
✅ TTL 5 phút trong Redis
✅ Token UUID để map với OTP
✅ Token bị xóa sau khi verify thành công
✅ Email validation qua external service
```

#### **4. API Protection - Tốt**
```
✅ @PreAuthorize cho role-based access
✅ JwtAuthenticationFilter
✅ Active status check
✅ UserPrincipal cho authentication context
```

#### **5. Third-party Authentication - Tốt**
```
✅ Firebase Auth cho Google OAuth
✅ Verify Firebase ID Token
✅ Auto-create user nếu chưa tồn tại
✅ Lưu authProvider (LOCAL/GOOGLE)
```

---

### **⚠️ ĐIỂM CẦN CẢI THIỆN**

#### **1. Thiếu 2FA thực sự** ❌ **Nghiêm trọng**
```
❌ KHÔNG có TOTP/Google Authenticator
❌ KHÔNG có SMS OTP khi login
❌ KHÔNG có QR code generation
❌ KHÔNG có backup codes
❌ KHÔNG có setting bật/tắt 2FA

Hiện tại chỉ có:
⚠️ OTP khi đăng ký (verify email)
⚠️ OTP khi quên mật khẩu
```

**Tác động:** Không đủ tiêu chí "Xác thực hai yếu tố" theo yêu cầu.

---

#### **2. Thiếu Logout API** ❌ **Nghiêm trọng**
```
❌ KHÔNG có endpoint /logout
❌ KHÔNG revoke refresh token khi logout
❌ Frontend tự xóa token (không an toàn)
❌ Refresh token vẫn valid cho đến khi hết hạn
```

**Tác động:** User không thể logout an toàn, token vẫn sử dụng được.

---

#### **3. Thiếu Security Features** ⚠️ **Cần có**
```
❌ KHÔNG có rate limiting (chống brute force)
❌ KHÔNG có CAPTCHA
❌ KHÔNG log failed login attempts
❌ KHÔNG có account lockout sau N lần sai
❌ KHÔNG có IP whitelist/blacklist
```

**Tác động:** Dễ bị tấn công brute force, không phát hiện suspicious activities.

---

#### **4. Session Management** ⚠️ **Nên có**
```
❌ KHÔNG track active sessions
❌ KHÔNG có "logout all devices"
❌ KHÔNG có "force logout" (admin)
❌ KHÔNG có session history
```

**Tác động:** Không quản lý được các phiên đăng nhập, không revoke tất cả token.

---

#### **5. Password Policy** ⚠️ **Yếu**
```
⚠️ CHỈ yêu cầu minimum 6 ký tự
❌ KHÔNG yêu cầu uppercase/lowercase
❌ KHÔNG yêu cầu số/ký tự đặc biệt
❌ KHÔNG kiểm tra password phổ biến
❌ KHÔNG có password strength meter
```

**Tác động:** Password yếu dễ bị crack.

---

#### **6. Audit Trail** ⚠️ **Nên có**
```
❌ KHÔNG log login/logout events
❌ KHÔNG log IP address, device info
❌ KHÔNG log failed attempts
❌ KHÔNG có activity log
```

**Tác động:** Không truy vết được hoạt động đáng ngờ.

---

## 🎯 KHUYẾN NGHỊ ĐỂ ĐẠT ĐIỂM TỐI ĐA

### **PRIORITY 1 - BẮT BUỘC** (để đạt 1.2-1.3 điểm)

#### **1. Thêm Logout API** ⏱️ **5 phút**

```java
@PostMapping("/api/auth/logout")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<ResponseMessage<String>> logout(
    HttpServletRequest request,
    HttpServletResponse response) {
    
    // Lấy refresh token từ cookie
    Cookie[] cookies = request.getCookies();
    String refreshToken = null;
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
    }
    
    // Xóa refresh token khỏi Redis
    if (refreshToken != null) {
        otpRedisService.deleteRefreshToken(refreshToken);
    }
    
    // Xóa cookie
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Đăng xuất thành công", null)
    );
}
```

**Test:**
```bash
POST /api/auth/logout
Authorization: Bearer <access-token>

→ Response: { "success": true, "message": "Đăng xuất thành công" }
→ Refresh token bị xóa khỏi Redis
→ Cookie bị xóa
```

---

#### **2. Thêm 2FA OTP khi Login** ⏱️ **30 phút**

**Bước 1: Thêm field vào User model**
```java
// User.java
private Boolean twoFactorEnabled = false;  // Mặc định tắt
```

**Bước 2: Thêm endpoint bật/tắt 2FA**
```java
@PutMapping("/api/users/toggle-2fa")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<ResponseMessage<String>> toggle2FA(
    @RequestBody Map<String, Boolean> request,
    @AuthenticationPrincipal UserPrincipal userPrincipal) {
    
    Boolean enabled = request.get("enabled");
    User user = userRepository.findById(userPrincipal.getId())
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    user.setTwoFactorEnabled(enabled);
    userRepository.save(user);
    
    return ResponseEntity.ok(new ResponseMessage<>(
        true, 
        enabled ? "Đã bật 2FA" : "Đã tắt 2FA", 
        null
    ));
}
```

**Bước 3: Sửa Login flow**
```java
public ResponseMessage<JwtResponse> login(LoginRequest request, HttpServletResponse response) {
    // ...existing validation...
    
    User user = optional.get();
    
    // Kiểm tra 2FA
    if (user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled()) {
        // Tạo OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        String token = UUID.randomUUID().toString();
        
        // Lưu email + OTP vào Redis
        String jsonData = String.format("""
            {
              "email": "%s",
              "otp": "%s"
            }
            """, user.getEmail(), otp);
        otpRedisService.saveOtp(token, jsonData, 5);
        
        // Gửi OTP qua email
        emailService.send2FAOtp(user.getEmail(), otp);
        
        // Trả về token để verify OTP
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("requires2FA", "true");
        
        return new ResponseMessage<>(true, "Vui lòng nhập OTP được gửi qua email", data);
    }
    
    // ...existing JWT generation...
}
```

**Bước 4: Thêm endpoint verify OTP login**
```java
@PostMapping("/api/auth/verify-login-otp")
public ResponseEntity<ResponseMessage<JwtResponse>> verifyLoginOtp(
    @RequestBody VerifyOtpRequest request,
    HttpServletResponse response) {
    
    String json = otpRedisService.getOtp(request.getToken());
    if (json == null) {
        return ResponseEntity.ok(
            new ResponseMessage<>(false, "Token hết hạn", null)
        );
    }
    
    // Parse JSON
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(json);
    String otpSaved = node.get("otp").asText();
    String email = node.get("email").asText();
    
    if (!otpSaved.equals(request.getOtp())) {
        return ResponseEntity.ok(
            new ResponseMessage<>(false, "OTP không đúng", null)
        );
    }
    
    // OTP đúng → tạo JWT
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    UserPrincipal userPrincipal = new UserPrincipal(
        user.getId(), user.getEmail(), user.getPassword(), 
        user.getRole(), user.isActive()
    );
    
    String accessToken = jwtService.generateAccessToken(userPrincipal);
    String refreshToken = UUID.randomUUID().toString();
    otpRedisService.saveRefreshToken(email, refreshToken, 10080);
    
    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60);
    response.addCookie(cookie);
    
    // Xóa OTP token
    otpRedisService.deleteOtp(request.getToken());
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Đăng nhập thành công", 
            new JwtResponse(accessToken))
    );
}
```

**Flow hoàn chỉnh:**
```
1. POST /api/auth/login
   { "email": "user@example.com", "password": "123456" }
   
   → Response: {
       "success": true,
       "message": "Vui lòng nhập OTP",
       "data": {
         "token": "uuid-token",
         "requires2FA": "true"
       }
     }

2. User nhận OTP qua email: 123456

3. POST /api/auth/verify-login-otp
   { "token": "uuid-token", "otp": "123456" }
   
   → Response: {
       "success": true,
       "message": "Đăng nhập thành công",
       "data": {
         "accessToken": "jwt-token"
       }
     }
```

---

### **PRIORITY 2 - NÊN CÓ** (để đạt 1.4-1.5 điểm)

#### **3. Rate Limiting** ⏱️ **20 phút**

Sử dụng Redis để track login attempts:

```java
@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isLoginAllowed(String email) {
        String key = "login_attempts:" + email;
        String attempts = redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            return true;
        }
        
        int count = Integer.parseInt(attempts);
        return count < 5;  // Giới hạn 5 lần
    }
    
    public void recordFailedLogin(String email) {
        String key = "login_attempts:" + email;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);  // Reset sau 15 phút
    }
    
    public void resetLoginAttempts(String email) {
        String key = "login_attempts:" + email;
        redisTemplate.delete(key);
    }
}
```

Sử dụng trong AuthService:
```java
public ResponseMessage<JwtResponse> login(LoginRequest request, HttpServletResponse response) {
    // Kiểm tra rate limit
    if (!rateLimitService.isLoginAllowed(request.getEmail())) {
        return new ResponseMessage<>(false, 
            "Tài khoản bị khóa 15 phút do đăng nhập sai nhiều lần", null);
    }
    
    Optional<User> optional = userRepository.findByEmail(request.getEmail());
    if (optional.isEmpty()) {
        rateLimitService.recordFailedLogin(request.getEmail());
        return new ResponseMessage<>(false, "Email không tồn tại!", null);
    }
    
    User user = optional.get();
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        rateLimitService.recordFailedLogin(request.getEmail());
        return new ResponseMessage<>(false, "Mật khẩu không đúng!", null);
    }
    
    // Login thành công → reset attempts
    rateLimitService.resetLoginAttempts(request.getEmail());
    
    // ...existing JWT generation...
}
```

---

#### **4. Login History** ⏱️ **15 phút**

```java
// LoginHistory.java
@Document(collection = "login_history")
@Data
public class LoginHistory {
    @Id
    private String id;
    private String userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
    private Boolean success;
}

// LoginHistoryRepository.java
public interface LoginHistoryRepository extends MongoRepository<LoginHistory, String> {
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(String userId);
}

// Trong AuthService
public void logLoginAttempt(String email, String ipAddress, String userAgent, boolean success) {
    User user = userRepository.findByEmail(email).orElse(null);
    
    LoginHistory history = new LoginHistory();
    history.setUserId(user != null ? user.getId() : null);
    history.setEmail(email);
    history.setIpAddress(ipAddress);
    history.setUserAgent(userAgent);
    history.setLoginTime(LocalDateTime.now());
    history.setSuccess(success);
    
    loginHistoryRepository.save(history);
}

// API xem lịch sử
@GetMapping("/api/users/login-history")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<ResponseMessage<List<LoginHistory>>> getLoginHistory(
    @AuthenticationPrincipal UserPrincipal userPrincipal) {
    
    List<LoginHistory> history = loginHistoryRepository
        .findByUserIdOrderByLoginTimeDesc(userPrincipal.getId());
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Lịch sử đăng nhập", history)
    );
}
```

---

#### **5. TOTP 2FA (Google Authenticator)** ⏱️ **1-2 giờ**

Sử dụng thư viện: `com.warrenstrange:googleauth:1.5.0`

```java
// Thêm vào User model
private String totpSecret;  // Secret key cho TOTP

// Service
@Service
public class TotpService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
    
    public String generateQRCodeUrl(String email, String secret) {
        return String.format(
            "otpauth://totp/%s?secret=%s&issuer=CourseApp",
            email, secret
        );
    }
    
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}

// API setup TOTP
@PostMapping("/api/users/setup-totp")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<ResponseMessage<Map<String, String>>> setupTotp(
    @AuthenticationPrincipal UserPrincipal userPrincipal) {
    
    User user = userRepository.findById(userPrincipal.getId())
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    String secret = totpService.generateSecret();
    String qrCodeUrl = totpService.generateQRCodeUrl(user.getEmail(), secret);
    
    user.setTotpSecret(secret);
    userRepository.save(user);
    
    Map<String, String> data = new HashMap<>();
    data.put("secret", secret);
    data.put("qrCodeUrl", qrCodeUrl);
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Scan QR code bằng Google Authenticator", data)
    );
}

// API verify TOTP
@PostMapping("/api/auth/verify-totp")
public ResponseEntity<ResponseMessage<JwtResponse>> verifyTotp(
    @RequestBody Map<String, Object> request) {
    
    String email = (String) request.get("email");
    int code = (int) request.get("code");
    
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (totpService.verifyCode(user.getTotpSecret(), code)) {
        // Generate JWT
        UserPrincipal userPrincipal = new UserPrincipal(
            user.getId(), user.getEmail(), user.getPassword(), 
            user.getRole(), user.isActive()
        );
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        
        return ResponseEntity.ok(
            new ResponseMessage<>(true, "Xác thực thành công", 
                new JwtResponse(accessToken))
        );
    } else {
        return ResponseEntity.ok(
            new ResponseMessage<>(false, "Mã TOTP không đúng", null)
        );
    }
}
```

---

## 📈 DỰ ĐOÁN ĐIỂM SAU KHI CẢI THIỆN

| Scenario | Chức năng thêm | Điểm ước tính | % |
|----------|----------------|---------------|---|
| **Hiện tại** | - | **0.96 / 1.5** | 64% |
| **+ Logout API** | Logout endpoint | **1.05 / 1.5** | 70% |
| **+ 2FA OTP Login** | OTP khi đăng nhập | **1.25 / 1.5** | 83% |
| **+ Rate Limiting** | Chống brute force | **1.32 / 1.5** | 88% |
| **+ Login History** | Audit trail | **1.38 / 1.5** | 92% |
| **+ TOTP 2FA** | Google Authenticator | **1.5 / 1.5** | 100% |

---

## 📋 CHECKLIST HÀNH ĐỘNG

### **Để đạt 1.2 điểm (80%):**
- [ ] **Thêm Logout API** (5 phút)
- [ ] **Thêm 2FA OTP khi Login** (30 phút)

### **Để đạt 1.35 điểm (90%):**
- [ ] Rate Limiting (20 phút)
- [ ] Login History (15 phút)

### **Để đạt 1.5 điểm (100%):**
- [ ] TOTP 2FA với Google Authenticator (1-2 giờ)
- [ ] Backup codes
- [ ] Session management
- [ ] Account lockout

---

## 🎯 KẾT LUẬN

### **Điểm mạnh hiện tại:**
✅ JWT implementation chuẩn chỉnh  
✅ OTP verification qua email  
✅ Google OAuth hoạt động tốt  
✅ Profile management đầy đủ  
✅ Password security tốt (BCrypt)  

### **Điểm yếu nghiêm trọng:**
❌ **THIẾU 2FA khi đăng nhập** (không đáp ứng yêu cầu "Xác thực hai yếu tố")  
❌ **THIẾU Logout API** (không đáp ứng yêu cầu "Đăng xuất an toàn")  

### **Ưu tiên tuyệt đối:**
🔴 **Thêm Logout API ngay** (5 phút làm được)  
🔴 **Thêm 2FA OTP khi Login** (30 phút làm được)  

**Với 2 chức năng trên, điểm sẽ tăng từ 0.96 lên ~1.25 (83%)**

---

**Tổng kết:**  
Hệ thống đã có nền tảng tốt nhưng thiếu 2 chức năng quan trọng. Nếu bổ sung **Logout** và **2FA Login**, có thể đạt **1.2-1.3 điểm**. Nếu thêm **Rate Limiting** và **Login History**, có thể đạt **1.35-1.4 điểm**. Để đạt **full 1.5 điểm**, cần triển khai **TOTP 2FA** (Google Authenticator).

---

**Ngày đánh giá:** 18/11/2025  
**Người đánh giá:** Backend Analysis Team  
**Phiên bản:** 1.0


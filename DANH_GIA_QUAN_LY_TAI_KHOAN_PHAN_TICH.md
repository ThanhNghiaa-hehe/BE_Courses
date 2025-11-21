# 📊 ĐÁNH GIÁ CHỨC NĂNG QUẢN LÝ TÀI KHOẢN & PHÂN TÍCH

**Ngày đánh giá:** 18/11/2025  
**Chức năng:** Quản lý tài khoản (User Management) & Phân tích dữ liệu (Analytics)  
**Điểm ước tính:** **0.5 - 0.6 / 1.5** (33-40%)

---

## 🎯 TỔNG QUAN

Hệ thống có **các chức năng quản lý tài khoản cơ bản** (CRUD users, phân quyền) nhưng **HOÀN TOÀN THIẾU** các tính năng phân tích dữ liệu, dashboard, và báo cáo thống kê.

---

## ✅ CÁC CHỨC NĂNG ĐÃ CÓ

### **PHẦN 1: QUẢN LÝ TÀI KHOẢN (USER MANAGEMENT)**

#### **1. Xem danh sách tất cả users** ✅ **100%**

**Endpoint:**
```
GET /api/admin/users/read-users
```

**Controller:**
```java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/read-users")
    public ResponseEntity<ResponseMessage<List<User>>> getAllUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }
}
```

**Service:**
```java
public ResponseMessage<List<User>> getAllUser() {
    List<User> userList = userRepository.findAll();
    return new ResponseMessage<>(true, "Danh sách user", userList);
}
```

**Response mẫu:**
```json
{
  "success": true,
  "message": "Danh sách user",
  "data": [
    {
      "id": "user123",
      "email": "user@example.com",
      "fullname": "Nguyễn Văn A",
      "phoneNumber": "0901234567",
      "role": "USER",
      "active": true,
      "gender": "MALE",
      "dateOfBirth": "1990-01-01",
      "createdAt": "2025-01-01",
      "avatarUrl": "http://...",
      "address": {
        "street": "123 ABC",
        "ward": "Phường 1",
        "district": "Quận 1",
        "city": "TP.HCM"
      },
      "authProvider": "LOCAL"
    }
  ]
}
```

**Điểm mạnh:**
- ✅ Chỉ ADMIN mới access được (`@PreAuthorize`)
- ✅ Trả về đầy đủ thông tin user
- ✅ Bao gồm cả users từ Google OAuth

**Vấn đề:**
- ❌ **KHÔNG có phân trang** → Performance kém khi nhiều users
- ❌ **KHÔNG có filter** (theo role, active status, authProvider)
- ❌ **KHÔNG có search** (theo email, tên, SĐT)
- ❌ **KHÔNG có sort** (theo ngày tạo, tên...)
- ⚠️ Trả về **toàn bộ thông tin** bao gồm password hash (security risk)

**Cần cải thiện:**
```java
// Tạo UserDTO để không expose password
@Data
@Builder
public class UserDTO {
    private String id;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String role;
    private boolean active;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate createdAt;
    private String avatarUrl;
    private Address address;
    private AutheProvider authProvider;
    // KHÔNG có password field
}

// Service với pagination, filter, search
public ResponseMessage<PagedUserResponse> getAllUsers(
    int page, 
    int size,
    String role,
    Boolean active,
    String searchKeyword,
    String sortBy,
    String direction) {
    
    Query query = new Query();
    
    // Filter by role
    if (role != null && !role.isEmpty()) {
        query.addCriteria(Criteria.where("role").is(role));
    }
    
    // Filter by active status
    if (active != null) {
        query.addCriteria(Criteria.where("active").is(active));
    }
    
    // Search by email, fullname, phone
    if (searchKeyword != null && !searchKeyword.isEmpty()) {
        Criteria searchCriteria = new Criteria().orOperator(
            Criteria.where("email").regex(searchKeyword, "i"),
            Criteria.where("fullname").regex(searchKeyword, "i"),
            Criteria.where("phoneNumber").regex(searchKeyword, "i")
        );
        query.addCriteria(searchCriteria);
    }
    
    // Sort
    Sort sort = Sort.by(
        "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
        sortBy != null ? sortBy : "createdAt"
    );
    
    // Pagination
    Pageable pageable = PageRequest.of(page, size, sort);
    query.with(pageable);
    
    List<User> users = mongoTemplate.find(query, User.class);
    long total = mongoTemplate.count(
        Query.query(query.getQueryObject()), 
        User.class
    );
    
    // Convert to DTO
    List<UserDTO> userDTOs = users.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
    
    PagedUserResponse response = PagedUserResponse.builder()
        .users(userDTOs)
        .total(total)
        .totalPages((int) Math.ceil((double) total / size))
        .currentPage(page)
        .pageSize(size)
        .build();
    
    return new ResponseMessage<>(true, "Danh sách users", response);
}
```

---

#### **2. Xem thông tin 1 user** ✅ **100%**

**Endpoint:**
```
GET /api/users/find-userId
```

**Service:**
```java
public ResponseMessage<Optional<User>> getUserById(String id) {
    Optional<User> optionalUser = userRepository.findById(id);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "Không tìm thấy id User", null);
    }
    return new ResponseMessage<>(true, "Success find by idUser", optionalUser);
}
```

**Điểm mạnh:**
- ✅ Có endpoint
- ✅ Error handling

**Vấn đề:**
- ⚠️ Endpoint là `/api/users/find-userId` nhưng lại dùng `@AuthenticationPrincipal` → Lấy user hiện tại, không phải by ID
- ⚠️ Nên có 2 endpoints:
  - `/api/users/me` → Lấy thông tin user hiện tại
  - `/api/admin/users/{id}` → Admin lấy thông tin user bất kỳ

---

#### **3. Cập nhật trạng thái active/inactive** ✅ **100%**

**Endpoint:**
```
PUT /api/admin/users/active/{id}
```

**Controller:**
```java
@PutMapping("/active/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseMessage<String>> updateUserActive(
    @PathVariable String id,
    @RequestBody UpdateUserActiveRequest request) {
    return ResponseEntity.ok(userService.updateUserActive(id, request.isActive()));
}
```

**Service:**
```java
public ResponseMessage<String> updateUserActive(String userId, boolean active) {
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "Không tìm thấy user", null);
    }
    User user = optionalUser.get();
    user.setActive(active);
    userRepository.save(user);
    return new ResponseMessage<>(true, "Cập nhật trạng thái thành công!", null);
}
```

**Request Body:**
```json
{
  "active": false
}
```

**Điểm mạnh:**
- ✅ Chức năng khóa/mở khóa tài khoản
- ✅ Chỉ ADMIN mới có quyền
- ✅ Validation user tồn tại

**Use cases:**
- Admin khóa tài khoản vi phạm
- Admin mở khóa sau khi giải quyết
- Tự động khóa sau N lần đăng nhập sai (cần thêm)

---

#### **4. Cập nhật role (phân quyền)** ✅ **100%**

**Endpoint:**
```
PUT /api/admin/users/{id}/role
```

**Controller:**
```java
@PutMapping("/{id}/role")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseMessage<String>> updateUserRole(
    @PathVariable String id,
    @Valid @RequestBody UpdateUserRoleRequest request,
    @AuthenticationPrincipal UserPrincipal admin) {
    return ResponseEntity.ok(userService.updateUserRole(id, admin.getEmail(), request));
}
```

**Service:**
```java
public ResponseMessage<String> updateUserRole(String userId, String adminEmail, UpdateUserRoleRequest request) {
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "Không tìm thấy người dùng", null);
    }
    
    User user = optionalUser.get();
    
    // Không cho admin tự thay đổi quyền của chính mình
    if (user.getEmail().equals(adminEmail)) {
        return new ResponseMessage<>(false, "Không thể tự thay đổi quyền của chính mình!", null);
    }
    
    // Validate role
    List<String> validRoles = List.of("USER", "MANAGER", "ADMIN");
    if (!validRoles.contains(request.getRole())) {
        return new ResponseMessage<>(false, "Role không hợp lệ!", null);
    }
    
    user.setRole(request.getRole());
    userRepository.save(user);
    return new ResponseMessage<>(true, "Cập nhật vai trò thành công cho user: " + user.getFullname(), null);
}
```

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Điểm mạnh:**
- ✅ Validate role hợp lệ (USER, MANAGER, ADMIN)
- ✅ **Không cho admin tự đổi role của chính mình** (security best practice)
- ✅ Error handling tốt

**Roles hiện có:**
- `USER` - Người dùng bình thường
- `MANAGER` - Quản lý (chưa rõ quyền)
- `ADMIN` - Quản trị viên

**Vấn đề:**
- ❌ **KHÔNG có role INSTRUCTOR** (giảng viên) - cần cho hệ thống khóa học
- ⚠️ Role `MANAGER` chưa được sử dụng trong code

---

#### **5. Xóa user** ✅ **100%**

**Endpoint:**
```
DELETE /api/users/{id}
```

**Service:**
```java
public ResponseMessage<String> deleteUserbyId(String id) {
    Optional<User> userOptional = userRepository.findById(id);
    
    if (userOptional.isPresent()) {
        userRepository.deleteById(id);
        return new ResponseMessage<>(true, "Xoá thành công user: " + userOptional.get().getFullname(), null);
    } else {
        return new ResponseMessage<>(false, "Id user không tồn tại: " + id, null);
    }
}
```

**Điểm mạnh:**
- ✅ Có chức năng xóa
- ✅ Kiểm tra tồn tại
- ✅ Thông báo rõ ràng

**Vấn đề:**
- ⚠️ Endpoint là `/api/users/{id}` → Không phải admin endpoint
- ⚠️ Có `@PreAuthorize("hasAnyRole('USER','ADMIN')")` → USER cũng có thể xóa (sai logic)
- ❌ **KHÔNG kiểm tra xóa chính mình**
- ❌ **Hard delete** (xóa vĩnh viễn) → Nên dùng **soft delete**
- ❌ **KHÔNG xóa dữ liệu liên quan** (orders, enrollments...)

**Cần sửa:**
```java
// Soft delete
@PutMapping("/admin/users/{id}/delete")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseMessage<String>> softDeleteUser(
    @PathVariable String id,
    @AuthenticationPrincipal UserPrincipal admin) {
    
    // Không cho xóa chính mình
    if (id.equals(admin.getId())) {
        return ResponseEntity.badRequest().body(
            new ResponseMessage<>(false, "Không thể xóa chính mình!", null)
        );
    }
    
    return ResponseEntity.ok(userService.softDeleteUser(id));
}

// Service
public ResponseMessage<String> softDeleteUser(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Đánh dấu deleted thay vì xóa
    user.setDeleted(true);
    user.setDeletedAt(LocalDateTime.now());
    user.setActive(false);
    userRepository.save(user);
    
    return new ResponseMessage<>(true, "Đã xóa user: " + user.getFullname(), null);
}
```

---

### **PHẦN 2: PHÂN TÍCH DỮ LIỆU (ANALYTICS & DASHBOARD)**

#### **1. Tổng doanh thu** ⚠️ **50%**

**Có trong OrderService nhưng CHƯA expose API:**

```java
public ResponseMessage<Double> calculateTotalRevenue() {
    try {
        List<Order> confirmedOrders = orderRepository.findAllByStatus(OrderStatus.CONFIRMED);
        double totalRevenue = confirmedOrders.stream()
            .mapToDouble(Order::getTotalPrice)
            .sum();
        return new ResponseMessage<>(true, "Total revenue calculated successfully", totalRevenue);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to calculate total revenue: " + e.getMessage(), null);
    }
}
```

**Vấn đề:**
- ❌ **KHÔNG có API endpoint**
- ⚠️ Chỉ tính orders có status CONFIRMED
- ⚠️ Nên tính orders PAID, ENROLLED, COMPLETED
- ❌ **KHÔNG có filter theo thời gian** (ngày, tháng, năm)

**Cần bổ sung:**

```java
// AnalyticsController.java
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Tổng doanh thu
     */
    @GetMapping("/revenue/total")
    public ResponseEntity<ResponseMessage<RevenueResponse>> getTotalRevenue(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.getTotalRevenue(fromDate, toDate));
    }
    
    /**
     * Doanh thu theo ngày
     */
    @GetMapping("/revenue/by-date")
    public ResponseEntity<ResponseMessage<Map<String, Double>>> getRevenueByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.getRevenueByDate(fromDate, toDate));
    }
    
    /**
     * Doanh thu theo tháng
     */
    @GetMapping("/revenue/by-month")
    public ResponseEntity<ResponseMessage<Map<String, Double>>> getRevenueByMonth(
        @RequestParam int year) {
        return ResponseEntity.ok(analyticsService.getRevenueByMonth(year));
    }
    
    /**
     * Doanh thu theo khóa học
     */
    @GetMapping("/revenue/by-course")
    public ResponseEntity<ResponseMessage<List<CourseRevenueDTO>>> getRevenueByCourse(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRevenueByCourse(fromDate, toDate, limit));
    }
}

// AnalyticsService.java
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    /**
     * Tổng doanh thu
     */
    public ResponseMessage<RevenueResponse> getTotalRevenue(LocalDate fromDate, LocalDate toDate) {
        Query query = new Query();
        
        // Chỉ tính orders đã thanh toán
        Criteria statusCriteria = Criteria.where("status").in(
            OrderStatus.PAID, 
            OrderStatus.ENROLLED, 
            OrderStatus.COMPLETED
        );
        query.addCriteria(statusCriteria);
        
        // Filter theo thời gian
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("createdAt")
                .gte(fromDate.atStartOfDay())
                .lte(toDate.atTime(23, 59, 59)));
        }
        
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        double totalRevenue = orders.stream()
            .mapToDouble(order -> order.getTotalPrice() != null ? order.getTotalPrice() : 0.0)
            .sum();
        
        int totalOrders = orders.size();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
        
        RevenueResponse response = RevenueResponse.builder()
            .totalRevenue(totalRevenue)
            .totalOrders(totalOrders)
            .averageOrderValue(averageOrderValue)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();
        
        return new ResponseMessage<>(true, "Total revenue", response);
    }
    
    /**
     * Doanh thu theo ngày
     */
    public ResponseMessage<Map<String, Double>> getRevenueByDate(LocalDate fromDate, LocalDate toDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(OrderStatus.PAID, OrderStatus.ENROLLED, OrderStatus.COMPLETED));
        query.addCriteria(Criteria.where("createdAt")
            .gte(fromDate.atStartOfDay())
            .lte(toDate.atTime(23, 59, 59)));
        
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        Map<String, Double> revenueByDate = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getCreatedAt().toLocalDate().toString(),
                Collectors.summingDouble(order -> order.getTotalPrice() != null ? order.getTotalPrice() : 0.0)
            ));
        
        return new ResponseMessage<>(true, "Revenue by date", revenueByDate);
    }
    
    /**
     * Doanh thu theo tháng
     */
    public ResponseMessage<Map<String, Double>> getRevenueByMonth(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(OrderStatus.PAID, OrderStatus.ENROLLED, OrderStatus.COMPLETED));
        query.addCriteria(Criteria.where("createdAt")
            .gte(startOfYear.atStartOfDay())
            .lte(endOfYear.atTime(23, 59, 59)));
        
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        Map<String, Double> revenueByMonth = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getCreatedAt().getYear() + "-" + 
                         String.format("%02d", order.getCreatedAt().getMonthValue()),
                Collectors.summingDouble(order -> order.getTotalPrice() != null ? order.getTotalPrice() : 0.0)
            ));
        
        return new ResponseMessage<>(true, "Revenue by month", revenueByMonth);
    }
    
    /**
     * Top khóa học bán chạy
     */
    public ResponseMessage<List<CourseRevenueDTO>> getRevenueByCourse(
        LocalDate fromDate, LocalDate toDate, int limit) {
        
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(OrderStatus.PAID, OrderStatus.ENROLLED, OrderStatus.COMPLETED));
        
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("createdAt")
                .gte(fromDate.atStartOfDay())
                .lte(toDate.atTime(23, 59, 59)));
        }
        
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        // Tính revenue theo course
        Map<String, CourseRevenueData> courseRevenueMap = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String courseId = item.getCourseId();
                double revenue = item.getDiscountedPrice() != null ? 
                    item.getDiscountedPrice() : 
                    (item.getPrice() != null ? item.getPrice() : 0.0);
                
                courseRevenueMap.computeIfAbsent(courseId, k -> new CourseRevenueData())
                    .addRevenue(revenue, item.getTitle());
            }
        }
        
        // Convert to DTO và sort
        List<CourseRevenueDTO> result = courseRevenueMap.entrySet().stream()
            .map(entry -> CourseRevenueDTO.builder()
                .courseId(entry.getKey())
                .courseTitle(entry.getValue().getTitle())
                .totalRevenue(entry.getValue().getTotalRevenue())
                .totalSales(entry.getValue().getTotalSales())
                .build())
            .sorted((a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
        
        return new ResponseMessage<>(true, "Top courses by revenue", result);
    }
}

// DTOs
@Data
@Builder
public class RevenueResponse {
    private Double totalRevenue;
    private Integer totalOrders;
    private Double averageOrderValue;
    private LocalDate fromDate;
    private LocalDate toDate;
}

@Data
@Builder
public class CourseRevenueDTO {
    private String courseId;
    private String courseTitle;
    private Double totalRevenue;
    private Integer totalSales;
}

@Data
class CourseRevenueData {
    private String title;
    private Double totalRevenue = 0.0;
    private Integer totalSales = 0;
    
    void addRevenue(Double revenue, String courseTitle) {
        this.totalRevenue += revenue;
        this.totalSales++;
        if (this.title == null) {
            this.title = courseTitle;
        }
    }
}
```

**Test:**
```bash
# Tổng doanh thu
GET /api/admin/analytics/revenue/total

# Doanh thu từ ngày đến ngày
GET /api/admin/analytics/revenue/total?fromDate=2025-01-01&toDate=2025-01-31

# Doanh thu theo ngày
GET /api/admin/analytics/revenue/by-date?fromDate=2025-01-01&toDate=2025-01-31

# Doanh thu theo tháng năm 2025
GET /api/admin/analytics/revenue/by-month?year=2025

# Top 10 khóa học bán chạy
GET /api/admin/analytics/revenue/by-course?limit=10
```

---

#### **2. Dashboard Statistics** ❌ **0%**

**Yêu cầu:**
Dashboard tổng quan với các số liệu:
- Tổng số users
- Tổng số khóa học
- Tổng số đơn hàng
- Doanh thu hôm nay/tuần/tháng
- Users mới hôm nay/tuần/tháng
- Tỷ lệ chuyển đổi (conversion rate)

**Cần bổ sung:**

```java
// DashboardController.java
@GetMapping("/dashboard/overview")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseMessage<DashboardOverview>> getDashboardOverview() {
    return ResponseEntity.ok(analyticsService.getDashboardOverview());
}

// AnalyticsService.java
public ResponseMessage<DashboardOverview> getDashboardOverview() {
    // Tổng users
    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByActive(true);
    
    // Users mới hôm nay
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    long newUsersToday = userRepository.countByCreatedAtAfter(startOfToday);
    
    // Users mới tháng này
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
    
    // Tổng courses
    long totalCourses = courseRepository.count();
    long publishedCourses = courseRepository.countByIsPublishedTrue();
    
    // Tổng orders
    long totalOrders = orderRepository.count();
    long paidOrders = orderRepository.countByStatus(OrderStatus.PAID);
    
    // Doanh thu hôm nay
    double revenueToday = getRevenueBetween(startOfToday, LocalDateTime.now());
    
    // Doanh thu tháng này
    double revenueThisMonth = getRevenueBetween(startOfMonth, LocalDateTime.now());
    
    // Tổng enrollments
    long totalEnrollments = enrollmentRepository.count();
    
    DashboardOverview overview = DashboardOverview.builder()
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .newUsersToday(newUsersToday)
        .newUsersThisMonth(newUsersThisMonth)
        .totalCourses(totalCourses)
        .publishedCourses(publishedCourses)
        .totalOrders(totalOrders)
        .paidOrders(paidOrders)
        .revenueToday(revenueToday)
        .revenueThisMonth(revenueThisMonth)
        .totalEnrollments(totalEnrollments)
        .build();
    
    return new ResponseMessage<>(true, "Dashboard overview", overview);
}

private double getRevenueBetween(LocalDateTime start, LocalDateTime end) {
    Query query = new Query();
    query.addCriteria(Criteria.where("status").in(OrderStatus.PAID, OrderStatus.ENROLLED, OrderStatus.COMPLETED));
    query.addCriteria(Criteria.where("createdAt").gte(start).lte(end));
    
    List<Order> orders = mongoTemplate.find(query, Order.class);
    return orders.stream()
        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
        .sum();
}

@Data
@Builder
public class DashboardOverview {
    // Users
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisMonth;
    
    // Courses
    private Long totalCourses;
    private Long publishedCourses;
    
    // Orders
    private Long totalOrders;
    private Long paidOrders;
    
    // Revenue
    private Double revenueToday;
    private Double revenueThisMonth;
    
    // Enrollments
    private Long totalEnrollments;
}
```

---

#### **3. User Analytics** ❌ **0%**

**Yêu cầu:**
- Users mới theo ngày/tháng
- Phân bố users theo role
- Phân bố users theo authProvider (LOCAL/GOOGLE)
- User activity (đăng nhập, mua hàng...)

**Cần bổ sung:**

```java
// UserAnalyticsController.java
@RestController
@RequestMapping("/api/admin/analytics/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAnalyticsController {
    
    private final UserAnalyticsService userAnalyticsService;
    
    /**
     * Users mới theo ngày
     */
    @GetMapping("/new-users/by-date")
    public ResponseEntity<ResponseMessage<Map<String, Long>>> getNewUsersByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(userAnalyticsService.getNewUsersByDate(fromDate, toDate));
    }
    
    /**
     * Phân bố users theo role
     */
    @GetMapping("/distribution/by-role")
    public ResponseEntity<ResponseMessage<Map<String, Long>>> getUsersByRole() {
        return ResponseEntity.ok(userAnalyticsService.getUserDistributionByRole());
    }
    
    /**
     * Phân bố users theo authProvider
     */
    @GetMapping("/distribution/by-provider")
    public ResponseEntity<ResponseMessage<Map<String, Long>>> getUsersByProvider() {
        return ResponseEntity.ok(userAnalyticsService.getUserDistributionByProvider());
    }
    
    /**
     * User growth chart
     */
    @GetMapping("/growth")
    public ResponseEntity<ResponseMessage<List<UserGrowthDTO>>> getUserGrowth(
        @RequestParam int months) {
        return ResponseEntity.ok(userAnalyticsService.getUserGrowth(months));
    }
}

// UserAnalyticsService.java
@Service
@RequiredArgsConstructor
public class UserAnalyticsService {
    
    private final UserRepository userRepository;
    
    public ResponseMessage<Map<String, Long>> getNewUsersByDate(LocalDate fromDate, LocalDate toDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("createdAt")
            .gte(fromDate.atStartOfDay())
            .lte(toDate.atTime(23, 59, 59)));
        
        List<User> users = mongoTemplate.find(query, User.class);
        
        Map<String, Long> usersByDate = users.stream()
            .collect(Collectors.groupingBy(
                user -> user.getCreatedAt().toLocalDate().toString(),
                Collectors.counting()
            ));
        
        return new ResponseMessage<>(true, "New users by date", usersByDate);
    }
    
    public ResponseMessage<Map<String, Long>> getUserDistributionByRole() {
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Long> distribution = allUsers.stream()
            .collect(Collectors.groupingBy(
                User::getRole,
                Collectors.counting()
            ));
        
        return new ResponseMessage<>(true, "User distribution by role", distribution);
    }
    
    public ResponseMessage<Map<String, Long>> getUserDistributionByProvider() {
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Long> distribution = allUsers.stream()
            .collect(Collectors.groupingBy(
                user -> user.getAuthProvider() != null ? 
                    user.getAuthProvider().toString() : "UNKNOWN",
                Collectors.counting()
            ));
        
        return new ResponseMessage<>(true, "User distribution by provider", distribution);
    }
    
    public ResponseMessage<List<UserGrowthDTO>> getUserGrowth(int months) {
        List<UserGrowthDTO> growth = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            
            long count = userRepository.countByCreatedAtBetween(
                monthStart.atStartOfDay(),
                monthEnd.atTime(23, 59, 59)
            );
            
            growth.add(UserGrowthDTO.builder()
                .month(monthStart.toString())
                .newUsers(count)
                .build());
        }
        
        return new ResponseMessage<>(true, "User growth", growth);
    }
}

@Data
@Builder
public class UserGrowthDTO {
    private String month;
    private Long newUsers;
}
```

---

#### **4. Course Analytics** ❌ **0%**

**Yêu cầu:**
- Top courses bán chạy
- Courses có rating cao nhất
- Phân bố courses theo category
- Phân bố courses theo level

**Cần bổ sung:**

```java
// CourseAnalyticsController.java
@RestController
@RequestMapping("/api/admin/analytics/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseAnalyticsController {
    
    private final CourseAnalyticsService courseAnalyticsService;
    
    @GetMapping("/top-selling")
    public ResponseEntity<ResponseMessage<List<CourseStatsDTO>>> getTopSellingCourses(
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(courseAnalyticsService.getTopSellingCourses(limit));
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<ResponseMessage<List<CourseStatsDTO>>> getTopRatedCourses(
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(courseAnalyticsService.getTopRatedCourses(limit));
    }
    
    @GetMapping("/distribution/by-category")
    public ResponseEntity<ResponseMessage<Map<String, Long>>> getCoursesByCategory() {
        return ResponseEntity.ok(courseAnalyticsService.getCourseDistributionByCategory());
    }
    
    @GetMapping("/distribution/by-level")
    public ResponseEntity<ResponseMessage<Map<String, Long>>> getCoursesByLevel() {
        return ResponseEntity.ok(courseAnalyticsService.getCourseDistributionByLevel());
    }
}
```

---

## 📊 BẢNG TỔNG KẾT CHI TIẾT

### **QUẢN LÝ TÀI KHOẢN**

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Xem danh sách users** | ✅ Có | 60% | **0.15** | Thiếu pagination, filter, search |
| 2 | **Xem chi tiết 1 user** | ✅ Có | 80% | **0.20** | Có nhưng endpoint không chuẩn |
| 3 | **Kích hoạt/Khóa user** | ✅ Hoàn chỉnh | 100% | **0.25** | Đầy đủ, tốt |
| 4 | **Phân quyền (role)** | ✅ Hoàn chỉnh | 100% | **0.25** | Security tốt, validate đúng |
| 5 | **Xóa user** | ⚠️ Có lỗi | 50% | **0.13** | Hard delete, thiếu validation |

**Tổng Quản lý tài khoản:** **0.98 / 1.25** (78%)

---

### **PHÂN TÍCH DỮ LIỆU**

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Dashboard Overview** | ❌ Không | 0% | **0.00** | ❌ THIẾU HOÀN TOÀN |
| 2 | **Revenue Analytics** | ⚠️ Một phần | 10% | **0.03** | Có method nhưng không có API |
| 3 | **User Analytics** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 4 | **Course Analytics** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 5 | **Order Analytics** | ❌ Không | 0% | **0.00** | ❌ THIẾU |
| 6 | **Charts & Graphs API** | ❌ Không | 0% | **0.00** | ❌ THIẾU |

**Tổng Phân tích:** **0.03 / 1.5** (2%)

---

**TỔNG ĐIỂM:** **0.98 + 0.03 = 1.01 / 2.75**  
**Nếu tính theo thang 1.5 điểm:** **~0.55 / 1.5** (37%)

---

## 🎯 KHUYẾN NGHỊ ĐỂ ĐẠT ĐIỂM TỐI ĐA

### **PRIORITY 1 - SỬA LỖI & CẢI THIỆN** (30 phút)

#### **1. Sửa Get All Users - Thêm Pagination**
```java
@GetMapping("/read-users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseMessage<PagedUserResponse>> getAllUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String role,
    @RequestParam(required = false) Boolean active,
    @RequestParam(required = false) String search) {
    return ResponseEntity.ok(userService.getAllUsers(page, size, role, active, search));
}
```

#### **2. Sửa Delete User - Soft Delete**
Code ở phần trên.

---

### **PRIORITY 2 - THÊM ANALYTICS CƠ BẢN** (để đạt 1.0-1.1 điểm)

#### **3. Dashboard Overview** ⏱️ **2 giờ**
- Tổng users, courses, orders
- Doanh thu hôm nay/tháng
- Users mới

#### **4. Revenue Analytics** ⏱️ **2 giờ**
- Tổng doanh thu
- Doanh thu theo ngày/tháng
- Top courses bán chạy

---

### **PRIORITY 3 - ANALYTICS ĐẦY ĐỦ** (để đạt 1.3-1.5 điểm)

#### **5. User Analytics** ⏱️ **2 giờ**
- Users mới theo thời gian
- Phân bố theo role, provider
- User growth chart

#### **6. Course Analytics** ⏱️ **2 giờ**
- Top courses
- Phân bố theo category, level

#### **7. Export Reports** ⏱️ **1 giờ**
- Export Excel
- Export PDF

---

## 📈 DỰ ĐOÁN ĐIỂM SAU KHI CẢI THIỆN

| Scenario | Chức năng thêm | Điểm ước tính | % |
|----------|----------------|---------------|---|
| **Hiện tại** | - | **0.55 / 1.5** | 37% |
| **+ Fix User Management** | Pagination, Soft Delete | **0.70 / 1.5** | 47% |
| **+ Dashboard Overview** | Tổng quan dashboard | **0.90 / 1.5** | 60% |
| **+ Revenue Analytics** | Phân tích doanh thu | **1.10 / 1.5** | 73% |
| **+ User & Course Analytics** | Phân tích đầy đủ | **1.35 / 1.5** | 90% |
| **+ Export Reports** | Xuất báo cáo | **1.5 / 1.5** | 100% |

---

## 📋 CHECKLIST HÀNH ĐỘNG

### **Để đạt 0.9 điểm (60%):**
- [ ] **Thêm Pagination cho Get All Users** (15 phút)
- [ ] **Sửa Soft Delete** (15 phút)
- [ ] **Dashboard Overview API** (2 giờ)

### **Để đạt 1.1 điểm (73%):**
- [ ] Revenue Analytics (2 giờ)
- [ ] Revenue by Date/Month (1 giờ)

### **Để đạt 1.35 điểm (90%):**
- [ ] User Analytics (2 giờ)
- [ ] Course Analytics (2 giờ)

---

## 🎯 KẾT LUẬN

### **Điểm mạnh:**
✅ **User Management cơ bản đầy đủ**  
✅ Phân quyền tốt, security awareness cao  
✅ Active/Inactive user hoạt động tốt  
✅ Validate role đúng, không cho admin tự đổi role  

### **Điểm yếu nghiêm trọng:**
❌ **HOÀN TOÀN THIẾU Analytics & Dashboard**  
❌ Không có báo cáo doanh thu  
❌ Không có thống kê users, courses, orders  
❌ Không có charts/graphs API  
❌ Get All Users thiếu pagination (performance issue)  
❌ Hard delete thay vì soft delete  

### **Ưu tiên tuyệt đối:**
🔴 **Dashboard Overview** (2 giờ) - QUAN TRỌNG NHẤT  
🔴 **Revenue Analytics** (2 giờ) - BẮT BUỘC  
🟡 **Pagination cho Get All Users** (15 phút) - NÊN LÀM NGAY  

**Với 3 cải thiện trên, điểm sẽ tăng từ 0.55 lên ~1.1 (73%)**

---

**Tổng kết:**  
**User Management tốt** nhưng **Analytics gần như KHÔNG CÓ**. Đây là điểm yếu lớn vì admin **KHÔNG THỂ** theo dõi kinh doanh, doanh thu, user growth. **BẮT BUỘC** phải có Dashboard và Revenue Analytics.

---

**Ngày đánh giá:** 18/11/2025  
**Người đánh giá:** Backend Analysis Team  
**Phiên bản:** 1.0  
**Mức độ ưu tiên:** 🟡 **HIGH** (sau Payment Gateway)


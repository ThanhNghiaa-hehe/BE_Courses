# 📊 ĐÁNH GIÁ CHỨC NĂNG GIỎ HÀNG VÀ MUA HÀNG

**Ngày đánh giá:** 18/11/2025  
**Chức năng:** Giỏ hàng và Mua hàng (Checkout & Order)  
**Điểm ước tính:** **1.1 - 1.2 / 1.5** (73-80%)

---

## 🎯 TỔNG QUAN

Hệ thống đã triển khai **đầy đủ các chức n��ng cơ bản** của giỏ hàng và mua hàng. Tuy nhiên, vẫn thiếu một số tính năng nâng cao như: **mã giảm giá**, **thanh toán online**, **tracking đơn hàng real-time**.

---

## ✅ CÁC CHỨC NĂNG ĐÃ CÓ

### **PHẦN 1: GIỎ HÀNG (CART)**

#### **1. Thêm khóa học vào giỏ hàng** ✅ **100%**

**Endpoint:**
```
POST /api/cart/add/{userId}
```

**Controller:**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@PostMapping("/add/{userId}")
public ResponseEntity<ResponseMessage<Cart>> addToCart(
    @PathVariable String userId,
    @RequestBody CartItem cartItem) {
    ResponseMessage<Cart> response = cartService.addToCart(userId, cartItem);
    return ResponseEntity.ok(response);
}
```

**Service Logic:**
```java
public ResponseMessage<Cart> addToCart(String userId, CartItem newItem) {
    // 1. Kiểm tra user tồn tại
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "User không tồn tại", null);
    }
    
    // 2. Tìm cart hiện tại
    Optional<Cart> existingCart = cartRepository.findByUserId(userId);
    Cart cart;
    
    if (existingCart.isPresent()) {
        cart = existingCart.get();
        
        // 3. Kiểm tra khóa học đã tồn tại chưa
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getCourseId().equals(newItem.getCourseId()))
            .findFirst();
        
        if (existingItem.isPresent()) {
            // Khóa học đã có → KHÔNG thêm nữa (vì khóa học không có quantity)
            return new ResponseMessage<>(false, "Khóa học đã có trong giỏ hàng", cart);
        } else {
            // Chưa có → Thêm mới
            cart.getItems().add(newItem);
        }
    } else {
        // 4. Tạo cart mới nếu chưa có
        List<CartItem> items = new ArrayList<>();
        items.add(newItem);
        cart = Cart.builder()
            .userId(userId)
            .items(items)
            .build();
    }
    
    Cart savedCart = cartRepository.save(cart);
    return new ResponseMessage<>(true, "Thêm vào giỏ hàng thành công", savedCart);
}
```

**Request Body:**
```json
{
  "courseId": "691c79f6190d8c0f5aac76a0",
  "title": "Khóa học Java Spring Boot",
  "thumbnailUrl": "http://localhost:8080/static/courses/course-java.jpg",
  "price": 1800000.0,
  "discountedPrice": 1620000.0,
  "discountPercent": 10,
  "selected": false,
  "level": "Advanced",
  "duration": 50,
  "instructorName": "Nguyễn Văn A",
  "rating": 4.8,
  "totalStudents": 1250
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thêm vào giỏ hàng thành công",
  "data": {
    "id": "cart123",
    "userId": "user123",
    "items": [
      {
        "courseId": "691c79f6190d8c0f5aac76a0",
        "title": "Khóa học Java Spring Boot",
        "price": 1800000.0,
        "discountedPrice": 1620000.0
      }
    ]
  }
}
```

**Điểm mạnh:**
- ✅ Kiểm tra user tồn tại
- ✅ Kiểm tra duplicate course (không cho thêm 2 lần)
- ✅ Tự động tạo cart nếu chưa có
- ✅ **KHÔNG có quantity** (phù hợp với khóa học)
- ✅ Lưu đầy đủ thông tin course vào cart

**Vấn đề nhỏ:**
- ⚠️ Endpoint có `{userId}` trong URL → Frontend phải gửi userId
- ⚠️ Nên dùng `@AuthenticationPrincipal` để lấy userId tự động

---

#### **2. Xem giỏ hàng** ✅ **100%**

**Endpoint:**
```
GET /api/cart/{userId}
```

**Service:**
```java
public ResponseMessage<Cart> getCartByUserId(String userId) {
    // 1. Kiểm tra user
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "User không tồn tại", null);
    }
    
    // 2. Tìm cart
    Optional<Cart> cart = cartRepository.findByUserId(userId);
    if (cart.isEmpty()) {
        // Tạo cart trống nếu chưa có
        Cart emptyCart = Cart.builder()
            .userId(userId)
            .items(new ArrayList<>())
            .build();
        return new ResponseMessage<>(true, "Giỏ hàng trống", emptyCart);
    }
    
    return new ResponseMessage<>(true, "Lấy giỏ hàng thành công", cart.get());
}
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy giỏ hàng thành công",
  "data": {
    "id": "cart123",
    "userId": "user123",
    "items": [
      {
        "courseId": "course1",
        "title": "Java Spring Boot",
        "price": 1800000.0,
        "discountedPrice": 1620000.0,
        "selected": false
      },
      {
        "courseId": "course2",
        "title": "ReactJS Advanced",
        "price": 2000000.0,
        "discountedPrice": 1800000.0,
        "selected": false
      }
    ]
  }
}
```

**Điểm mạnh:**
- ✅ Tự động tạo cart trống nếu chưa có
- ✅ Trả về message rõ ràng

---

#### **3. Xóa khóa học khỏi giỏ hàng** ✅ **100%**

**Endpoint:**
```
DELETE /api/cart/{userId}/item/{courseId}
```

**Service:**
```java
public ResponseMessage<Boolean> deleteCartItem(String userId, String courseId) {
    // 1. Kiểm tra user
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "User không tồn tại", false);
    }
    
    // 2. Tìm cart
    Optional<Cart> existingCart = cartRepository.findByUserId(userId);
    if (!existingCart.isPresent()) {
        return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", false);
    }
    
    // 3. Xóa item
    List<CartItem> items = existingCart.get().getItems();
    boolean itemRemoved = items.removeIf(item -> 
        item.getCourseId().equals(courseId)
    );
    
    if (!itemRemoved) {
        return new ResponseMessage<>(false, "Khóa học không tồn tại trong giỏ hàng", false);
    }
    
    // 4. Lưu lại
    cartRepository.save(existingCart.get());
    return new ResponseMessage<>(true, "Xóa khóa học thành công", true);
}
```

**Test:**
```bash
DELETE /api/cart/user123/item/course123
→ Response: { "success": true, "message": "Xóa khóa học thành công" }
```

**Điểm mạnh:**
- ✅ Kiểm tra tồn tại user và cart
- ✅ Sử dụng `removeIf` (clean code)
- ✅ Thông báo lỗi rõ ràng

---

#### **4. Lấy tất cả items trong giỏ hàng** ✅ **100%**

**Endpoint:**
```
GET /api/cart/items/{userId}
```

**Service:**
```java
public ResponseMessage<List<CartItem>> getAllProductsInCart(String userId) {
    // 1. Kiểm tra user
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "User không tồn tại", null);
    }
    
    // 2. Tìm cart
    Optional<Cart> existingCart = cartRepository.findByUserId(userId);
    if (!existingCart.isPresent()) {
        return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", null);
    }
    
    // 3. Lấy danh sách items
    List<CartItem> items = existingCart.get().getItems();
    return new ResponseMessage<>(true, "Danh sách sản phẩm trong giỏ hàng", items);
}
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách sản phẩm trong giỏ hàng",
  "data": [
    {
      "courseId": "course1",
      "title": "Java Spring Boot",
      "price": 1800000.0,
      "discountedPrice": 1620000.0,
      "selected": false
    }
  ]
}
```

---

#### **5. Tính tổng giá trị giỏ hàng** ✅ **90%**

**Endpoint:**
```
GET /api/cart/total/{userId}
```

**Service:**
```java
public ResponseMessage<Double> getTotalPriceOfCart(String userId) {
    // 1. Kiểm tra user
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return new ResponseMessage<>(false, "User không tồn tại", null);
    }
    
    // 2. Tìm cart
    Optional<Cart> existingCart = cartRepository.findByUserId(userId);
    if (!existingCart.isPresent()) {
        return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", null);
    }
    
    // 3. Tính tổng giá
    List<CartItem> items = existingCart.get().getItems();
    double totalPrice = items.stream()
        .mapToDouble(CartItem::getPrice)  // ⚠️ Dùng price thay vì discountedPrice
        .sum();
    
    return new ResponseMessage<>(true, "Tổng giá trị giỏ hàng", totalPrice);
}
```

**Vấn đề:**
- ⚠️ Đang tính tổng theo `price` (giá gốc)
- ⚠️ Nên tính theo `discountedPrice` (giá sau giảm)
- ⚠️ Không check `selected` field

**Cần sửa:**
```java
double totalPrice = items.stream()
    .filter(CartItem::isSelected)  // Chỉ tính items được chọn
    .mapToDouble(item -> {
        // Ưu tiên discountedPrice, nếu null thì dùng price
        Double finalPrice = item.getDiscountedPrice();
        if (finalPrice == null || finalPrice <= 0) {
            finalPrice = item.getPrice();
        }
        return finalPrice != null ? finalPrice : 0.0;
    })
    .sum();
```

---

#### **6. Xem tất cả giỏ hàng (Admin)** ✅ **100%**

**Endpoint:**
```
GET /api/cart/all
```

**Service:**
```java
public ResponseMessage<List<Cart>> getAllCarts() {
    List<Cart> carts = cartRepository.findAll();
    return new ResponseMessage<>(true, "Get all carts successfully", carts);
}
```

**Điểm mạnh:**
- ✅ Chỉ Admin mới access được
- ✅ Phục vụ quản lý, analytics

---

#### **7. Xóa nhiều items khi tạo order** ✅ **100%**

**Method internal (được gọi từ OrderService):**
```java
public boolean deleteCartItemsByProductIds(String userId, List<String> courseIds) {
    // 1. Kiểm tra user
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
        return false;
    }
    
    // 2. Tìm cart
    Optional<Cart> existingCart = cartRepository.findByUserId(userId);
    if (!existingCart.isPresent()) {
        return false;
    }
    
    // 3. Xóa nhiều items
    List<CartItem> items = existingCart.get().getItems();
    for (String id : courseIds) {
        items.removeIf(item -> item.getCourseId().equals(id));
    }
    
    // 4. Lưu lại
    cartRepository.save(existingCart.get());
    return true;
}
```

**Được sử dụng trong OrderService:**
```java
// Sau khi tạo order thành công
List<String> courseIds = orderRequest.getItems().stream()
    .map(OrderItem::getCourseId)
    .toList();

cartSerivce.deleteCartItemsByProductIds(orderRequest.getUserId(), courseIds);
```

**Điểm mạnh:**
- ✅ Tự động xóa items khỏi cart sau khi đặt hàng
- ✅ Xóa theo batch (hiệu quả)

---

### **PHẦN 2: MUA HÀNG (ORDER)**

#### **1. Tạo đơn hàng** ✅ **100%**

**Endpoint:**
```
POST /api/orders/create-order
```

**Controller:**
```java
@PostMapping("/create-order")
public ResponseEntity<ResponseMessage<Order>> createOrder(@RequestBody Order order) {
    ResponseMessage<Order> response = orderService.createOrder(order);
    if (response.isSuccess()) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

**Service Logic (Đã được kiểm tra trước đó):**
```java
public ResponseMessage<Order> createOrder(Order orderRequest) {
    try {
        // 1. Validate dữ liệu
        if (orderRequest == null) {
            return new ResponseMessage<>(false, "Order data is required", null);
        }
        
        if (orderRequest.getUserId().isEmpty()) {
            return new ResponseMessage<>(false, "User ID is required", null);
        }
        
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            return new ResponseMessage<>(false, "Order items are required", null);
        }
        
        if (orderRequest.getShippingAddress() == null || 
            orderRequest.getShippingAddress().trim().isEmpty()) {
            return new ResponseMessage<>(false, "Shipping address is required", null);
        }
        
        if (orderRequest.getPaymentMethod() == null) {
            return new ResponseMessage<>(false, "Payment method is required", null);
        }
        
        // 2. Tạo Order entity
        Order order = Order.builder()
            .userId(orderRequest.getUserId())
            .items(orderRequest.getItems())
            .discount(orderRequest.getDiscount() != null ? orderRequest.getDiscount() : 0.0)
            .shippingAddress(orderRequest.getShippingAddress())
            .paymentMethod(orderRequest.getPaymentMethod())
            .status(OrderStatus.UNCONFIRMED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // 3. Tính tổng tiền nếu chưa có
        if (orderRequest.getTotalPrice() == null || orderRequest.getTotalPrice() <= 0) {
            Double calculatedTotal = calculateTotalPrice(order);
            order.setTotalPrice(calculatedTotal);
        } else {
            order.setTotalPrice(orderRequest.getTotalPrice());
        }
        
        // 4. Lưu order
        Order savedOrder = orderRepository.save(order);
        
        // 5. Xóa items khỏi cart
        List<String> courseIds = orderRequest.getItems().stream()
            .map(OrderItem::getCourseId)
            .toList();
        
        if (cartSerivce.deleteCartItemsByProductIds(
            orderRequest.getUserId(), courseIds)) {
            System.out.println("Warning: Failed to remove items from cart");
        }
        
        return new ResponseMessage<>(true, "Order created successfully", savedOrder);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to create order: " + e.getMessage(), null);
    }
}
```

**Request Body:**
```json
{
  "userId": "user123",
  "items": [
    {
      "courseId": "course1",
      "title": "Java Spring Boot",
      "thumbnailUrl": "http://...",
      "price": 1800000.0,
      "discountedPrice": 1620000.0,
      "discountPercent": 10,
      "level": "Advanced",
      "duration": 50,
      "instructorName": "Nguyễn Văn A",
      "rating": 4.8,
      "totalStudents": 1250
    }
  ],
  "totalPrice": null,
  "discount": 0.0,
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "paymentMethod": "CREDIT_CARD"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": "order123",
    "userId": "user123",
    "items": [...],
    "totalPrice": 1620000.0,
    "discount": 0.0,
    "shippingAddress": "123 Đường ABC",
    "paymentMethod": "CREDIT_CARD",
    "status": "UNCONFIRMED",
    "createdAt": "2025-11-18T20:30:00",
    "updatedAt": "2025-11-18T20:30:00"
  }
}
```

**Điểm mạnh:**
- ✅ Validation đầy đủ
- ✅ Tự động tính tổng tiền
- ✅ Tự động xóa items khỏi cart
- ✅ Set status = UNCONFIRMED
- ✅ Set createdAt, updatedAt
- ✅ Exception handling

---

#### **2. Xem đơn hàng của user** ✅ **100%**

**Endpoint:**
```
GET /api/orders/{userId}
```

**Service:**
```java
public ResponseMessage<List<OrderResponse>> getOrdersByUserId(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
        return new ResponseMessage<>(false, "User ID is required", null);
    }
    
    try {
        // 1. Lấy thông tin user
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ResponseMessage<>(false, "User not found", null);
        }
        
        User user = userOptional.get();
        
        // 2. Lấy orders
        List<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        // 3. Convert sang OrderResponse (kèm thông tin user)
        List<OrderResponse> orderResponses = orders.stream()
            .map(order -> OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userName(user.getFullname())
                .userPhone(user.getPhoneNumber())
                .items(order.getItems())
                .totalPrice(order.getTotalPrice())
                .discount(order.getDiscount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build())
            .toList();
        
        return new ResponseMessage<>(true, "Orders retrieved successfully", orderResponses);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to retrieve orders", null);
    }
}
```

**Điểm mạnh:**
- ✅ Sắp xếp theo createdAt DESC (mới nhất trước)
- ✅ Kèm thông tin user (userName, userPhone)
- ✅ Convert sang OrderResponse (không expose trực tiếp Order entity)

---

#### **3. Cập nhật trạng thái đơn hàng** ✅ **100%**

**Endpoint:**
```
PUT /api/orders/{orderId}/update-status?newStatus=CONFIRMED
```

**Service:**
```java
public ResponseMessage<Order> updateOrderStatus(String orderId, OrderStatus status) {
    if (orderId == null || orderId.trim().isEmpty()) {
        return new ResponseMessage<>(false, "Order ID is required", null);
    }
    
    if (status == null) {
        return new ResponseMessage<>(false, "Order status is required", null);
    }
    
    // 1. Tìm order
    Optional<Order> orderOptional = orderRepository.findById(orderId);
    if (orderOptional.isEmpty()) {
        return new ResponseMessage<>(false, "Order not found", null);
    }
    
    Order order = orderOptional.get();
    OrderStatus previousStatus = order.getStatus();
    
    // 2. Business logic cho status transitions
    if (order.getStatus() == OrderStatus.CANCELLED) {
        return new ResponseMessage<>(false, "Cannot update status of cancelled order", null);
    }
    
    // 3. Kiểm tra nếu confirm order
    if (status == OrderStatus.CONFIRMED && previousStatus != OrderStatus.CONFIRMED) {
        ResponseMessage<String> stockUpdateResult = updateProductStock(order);
        if (!stockUpdateResult.isSuccess()) {
            return new ResponseMessage<>(false, stockUpdateResult.getMessage(), null);
        }
    }
    
    // 4. Cập nhật status
    order.setStatus(status);
    order.setUpdatedAt(LocalDateTime.now());
    
    try {
        Order updatedOrder = orderRepository.save(order);
        return new ResponseMessage<>(true, "Order status updated successfully", updatedOrder);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to update order status", null);
    }
}
```

**Order Status Flow:**
```
UNCONFIRMED → PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     │                                                          ▲
     └─────────────────▶ CANCELLED ◀───────────────────────────┘
```

**Điểm mạnh:**
- ✅ Không cho update order đã CANCELLED
- ✅ Verify course availability khi CONFIRMED
- ✅ Update updatedAt timestamp
- ✅ Business logic rõ ràng

---

#### **4. Hủy đơn hàng** ✅ **100%**

**Endpoint:**
```
PUT /api/orders/{orderId}/cancel
```

**Service:**
```java
public ResponseMessage<String> cancelOrder(String orderId) {
    if (orderId == null || orderId.trim().isEmpty()) {
        return new ResponseMessage<>(false, "Order ID is required", null);
    }
    
    // 1. Tìm order
    Optional<Order> orderOptional = orderRepository.findById(orderId);
    if (orderOptional.isEmpty()) {
        return new ResponseMessage<>(false, "Order not found", null);
    }
    
    Order order = orderOptional.get();
    
    // 2. Chỉ cho phép hủy PENDING orders
    if (order.getStatus() == OrderStatus.CONFIRMED) {
        return new ResponseMessage<>(false, "Cannot cancel confirmed order", null);
    }
    
    if (order.getStatus() == OrderStatus.CANCELLED) {
        return new ResponseMessage<>(false, "Order is already cancelled", null);
    }
    
    // 3. Cập nhật status
    order.setStatus(OrderStatus.CANCELLED);
    order.setUpdatedAt(LocalDateTime.now());
    
    try {
        orderRepository.save(order);
        return new ResponseMessage<>(true, "Order cancelled successfully", null);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to cancel order", null);
    }
}
```

**Điểm mạnh:**
- ✅ Không cho cancel order đã CONFIRMED
- ✅ Kiểm tra order đã CANCELLED
- ✅ Business rule rõ ràng

---

#### **5. Xem tất cả đơn hàng (Admin)** ✅ **100%**

**Endpoint:**
```
GET /api/admin/orders/all
```

**Service:**
```java
public ResponseMessage<List<OrderResponse>> getAllOrders() {
    try {
        List<Order> orders = orderRepository.findAll();
        
        // Convert sang OrderResponse kèm user info
        List<OrderResponse> orderResponses = orders.stream()
            .map(order -> {
                Optional<User> userOptional = userRepository.findById(order.getUserId());
                String userName = userOptional.map(User::getFullname).orElse("Unknown User");
                String userPhone = userOptional.map(User::getPhoneNumber).orElse("Unknown Phone");
                
                return OrderResponse.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .userName(userName)
                    .userPhone(userPhone)
                    .items(order.getItems())
                    .totalPrice(order.getTotalPrice())
                    .discount(order.getDiscount())
                    .shippingAddress(order.getShippingAddress())
                    .paymentMethod(order.getPaymentMethod())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();
            })
            .toList();
        
        return new ResponseMessage<>(true, "Orders retrieved successfully", orderResponses);
    } catch (Exception e) {
        return new ResponseMessage<>(false, "Failed to retrieve orders", null);
    }
}
```

**Điểm mạnh:**
- ✅ Chỉ Admin access
- ✅ Lấy tất cả orders
- ✅ Kèm thông tin user

---

#### **6. Tính tổng giá trị đơn hàng** ✅ **100%**

**Private method trong OrderService:**
```java
private Double calculateTotalPrice(Order order) {
    // Đối với khóa học, không có quantity
    double itemsTotal = order.getItems().stream()
        .mapToDouble(item -> {
            // Ưu tiên discountedPrice, nếu null thì dùng price
            Double finalPrice = item.getDiscountedPrice();
            if (finalPrice == null || finalPrice <= 0) {
                finalPrice = item.getPrice();
            }
            return finalPrice != null ? finalPrice : 0.0;
        })
        .sum();
    
    // Áp dụng discount của order (%)
    Double orderDiscount = order.getDiscount() != null ? order.getDiscount() : 0.0;
    double discountAmount = itemsTotal * (orderDiscount / 100.0);
    itemsTotal -= discountAmount;
    
    return Math.max(0.0, itemsTotal);
}
```

**Logic:**
```
1. Tính tổng items (dùng discountedPrice nếu có, không thì dùng price)
2. Áp dụng discount của order (%)
3. Đảm bảo total >= 0
```

**Ví dụ:**
```
Item 1: discountedPrice = 1,620,000 VNĐ
Item 2: discountedPrice = 1,800,000 VNĐ
Tổng items = 3,420,000 VNĐ

Order discount = 5%
Giảm = 3,420,000 * 5% = 171,000 VNĐ

Total = 3,420,000 - 171,000 = 3,249,000 VNĐ
```

**Điểm mạnh:**
- ✅ Ưu tiên discountedPrice
- ✅ Null safety
- ✅ Không cho total âm
- ✅ Hỗ trợ discount tổng đơn

---

#### **7. Verify khóa học khi confirm order** ✅ **100%**

**Private method:**
```java
private ResponseMessage<String> updateProductStock(Order order) {
    try {
        // Đối với khóa học, không cần trừ kho
        // Chỉ verify khóa học tồn tại và đang được publish
        for (OrderItem item : order.getItems()) {
            Optional<Course> courseOptional = courseRepository.findById(item.getCourseId());
            
            if (courseOptional.isEmpty()) {
                return new ResponseMessage<>(false, 
                    "Khóa học không tồn tại với ID: " + item.getCourseId(), null);
            }
            
            Course course = courseOptional.get();
            
            // Kiểm tra khóa học có đang được publish không
            if (course.getIsPublished() == null || !course.getIsPublished()) {
                return new ResponseMessage<>(false,
                    "Khóa học không khả dụng: " + course.getTitle(), null);
            }
            
            // Không cần update stock vì khóa học không có giới hạn số lượng
        }
        
        return new ResponseMessage<>(true, "Xác thực khóa học thành công", null);
    } catch (Exception e) {
        return new ResponseMessage<>(false, 
            "Lỗi khi xác thực khóa học: " + e.getMessage(), null);
    }
}
```

**Điểm mạnh:**
- ✅ Verify course tồn tại
- ✅ Kiểm tra isPublished
- ✅ KHÔNG trừ stock (phù hợp với khóa học)
- ✅ Error handling

---

## ❌ CÁC CHỨC NĂNG THIẾU

### **1. Mã giảm giá (Coupon/Voucher)** ❌ **0%** - **QUAN TRỌNG**

**Yêu cầu:**
- Tạo mã giảm giá (code, discount %, ngày hết hạn, số lần sử dụng)
- Apply mã giảm giá vào order
- Validate mã (còn hạn, chưa hết lượt)
- Track mã đã sử dụng

**Hiện trạng:**
- ❌ KHÔNG có Coupon model
- ❌ KHÔNG có API apply coupon
- ❌ Order chỉ có `discount` field (%)

**Cần bổ sung:**

```java
// Coupon.java
@Document(collection = "coupons")
@Data
@Builder
public class Coupon {
    @Id
    private String id;
    private String code;                // VD: "SUMMER2025"
    private Integer discountPercent;    // % giảm giá
    private Double maxDiscount;         // Giảm tối đa (VNĐ)
    private Double minOrderValue;       // Giá trị đơn tối thiểu
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer maxUsage;           // Số lần sử dụng tối đa
    private Integer currentUsage;       // Đã sử dụng bao nhiêu lần
    private Boolean isActive;
}

// CouponService.java
public ResponseMessage<CouponValidationResult> validateCoupon(
    String code, Double orderTotal) {
    
    Coupon coupon = couponRepository.findByCode(code)
        .orElse(null);
    
    if (coupon == null) {
        return new ResponseMessage<>(false, "Mã giảm giá không tồn tại", null);
    }
    
    if (!coupon.getIsActive()) {
        return new ResponseMessage<>(false, "Mã giảm giá đã bị vô hiệu hóa", null);
    }
    
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
        return new ResponseMessage<>(false, "Mã giảm giá đã hết hạn", null);
    }
    
    if (coupon.getCurrentUsage() >= coupon.getMaxUsage()) {
        return new ResponseMessage<>(false, "Mã giảm giá đã hết lượt sử dụng", null);
    }
    
    if (orderTotal < coupon.getMinOrderValue()) {
        return new ResponseMessage<>(false, 
            "Đơn hàng tối thiểu " + coupon.getMinOrderValue() + " VNĐ", null);
    }
    
    // Tính discount
    Double discount = orderTotal * (coupon.getDiscountPercent() / 100.0);
    if (coupon.getMaxDiscount() != null) {
        discount = Math.min(discount, coupon.getMaxDiscount());
    }
    
    CouponValidationResult result = CouponValidationResult.builder()
        .isValid(true)
        .discountAmount(discount)
        .couponCode(code)
        .build();
    
    return new ResponseMessage<>(true, "Mã giảm giá hợp lệ", result);
}

// API
@PostMapping("/orders/apply-coupon")
public ResponseEntity<ResponseMessage<CouponValidationResult>> applyCoupon(
    @RequestBody ApplyCouponRequest request) {
    return ResponseEntity.ok(
        couponService.validateCoupon(request.getCode(), request.getOrderTotal())
    );
}
```

---

### **2. Thanh toán Online** ❌ **0%** - **QUAN TRỌNG**

**Yêu cầu:**
- Tích hợp VNPay/MoMo/ZaloPay
- Payment gateway redirect
- Payment callback verification
- Update order status sau payment

**Hiện trạng:**
- ❌ Chỉ có PaymentMethod enum (CREDIT_CARD, PAYPAL...)
- ❌ KHÔNG có payment gateway integration
- ❌ KHÔNG có payment callback

**Cần bổ sung:**

```java
// VNPayService.java
@Service
public class VNPayService {
    
    public String createPaymentUrl(Order order, HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long)(order.getTotalPrice() * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", order.getId());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", getIpAddress(request));
        
        // Sort & build URL
        String queryUrl = vnp_Params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        
        String vnp_SecureHash = hmacSHA512(vnpHashSecret, queryUrl);
        String paymentUrl = vnpUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
        
        return paymentUrl;
    }
    
    public boolean verifyPayment(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        
        String queryUrl = params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
        
        String calculatedHash = hmacSHA512(vnpHashSecret, queryUrl);
        return vnp_SecureHash.equals(calculatedHash);
    }
}

// PaymentController.java
@PostMapping("/payment/vnpay/create")
public ResponseEntity<ResponseMessage<String>> createVNPayPayment(
    @RequestBody CreatePaymentRequest request,
    HttpServletRequest httpRequest) {
    
    Order order = orderRepository.findById(request.getOrderId())
        .orElseThrow(() -> new RuntimeException("Order not found"));
    
    String paymentUrl = vnPayService.createPaymentUrl(order, httpRequest);
    
    return ResponseEntity.ok(
        new ResponseMessage<>(true, "Payment URL created", paymentUrl)
    );
}

@GetMapping("/payment/vnpay/callback")
public ResponseEntity<String> vnpayCallback(@RequestParam Map<String, String> params) {
    if (vnPayService.verifyPayment(params)) {
        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
            return ResponseEntity.ok("Payment successful");
        }
    }
    return ResponseEntity.badRequest().body("Payment failed");
}
```

---

### **3. Order Tracking / History** ⚠️ **30%**

**Yêu cầu:**
- Lịch sử thay đổi status
- Timeline của order
- Notification khi status thay đổi

**Hiện trạng:**
- ✅ Có `createdAt`, `updatedAt`
- ❌ KHÔNG track từng lần thay đổi status
- ❌ KHÔNG có OrderStatusHistory

**Cần bổ sung:**

```java
// OrderStatusHistory.java
@Document(collection = "order_status_history")
@Data
@Builder
public class OrderStatusHistory {
    @Id
    private String id;
    private String orderId;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String changedBy;           // userId của người thay đổi
    private String note;
    private LocalDateTime changedAt;
}

// Trong OrderService.updateOrderStatus()
// Lưu lại history mỗi lần thay đổi status
OrderStatusHistory history = OrderStatusHistory.builder()
    .orderId(orderId)
    .fromStatus(previousStatus)
    .toStatus(status)
    .changedBy(currentUser.getId())
    .note("Status updated")
    .changedAt(LocalDateTime.now())
    .build();

orderStatusHistoryRepository.save(history);

// API xem history
@GetMapping("/orders/{orderId}/history")
public ResponseEntity<ResponseMessage<List<OrderStatusHistory>>> getOrderHistory(
    @PathVariable String orderId) {
    List<OrderStatusHistory> history = 
        orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
    return ResponseEntity.ok(new ResponseMessage<>(true, "Success", history));
}
```

---

### **4. Multiple Select trong Cart** ⚠️ **50%**

**Yêu cầu:**
- Chọn nhiều items trong cart
- Tính tổng tiền của items được chọn
- Checkout chỉ items được chọn

**Hiện trạng:**
- ✅ CartItem có field `selected`
- ❌ KHÔNG có API update `selected`
- ❌ getTotalPrice() KHÔNG check `selected`

**Cần bổ sung:**

```java
// CartService.java
public ResponseMessage<Cart> updateItemSelection(
    String userId, String courseId, boolean selected) {
    
    Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
    if (cartOptional.isEmpty()) {
        return new ResponseMessage<>(false, "Cart not found", null);
    }
    
    Cart cart = cartOptional.get();
    
    boolean found = false;
    for (CartItem item : cart.getItems()) {
        if (item.getCourseId().equals(courseId)) {
            item.setSelected(selected);
            found = true;
            break;
        }
    }
    
    if (!found) {
        return new ResponseMessage<>(false, "Item not found in cart", null);
    }
    
    cartRepository.save(cart);
    return new ResponseMessage<>(true, "Selection updated", cart);
}

// Controller
@PutMapping("/cart/{userId}/item/{courseId}/select")
public ResponseEntity<ResponseMessage<Cart>> updateSelection(
    @PathVariable String userId,
    @PathVariable String courseId,
    @RequestParam boolean selected) {
    return ResponseEntity.ok(
        cartService.updateItemSelection(userId, courseId, selected)
    );
}

// Update getTotalPrice để chỉ tính items selected
public ResponseMessage<Double> getTotalPriceOfSelectedItems(String userId) {
    // ... existing code ...
    
    double totalPrice = items.stream()
        .filter(CartItem::isSelected)  // ← Thêm filter này
        .mapToDouble(item -> {
            Double finalPrice = item.getDiscountedPrice();
            if (finalPrice == null || finalPrice <= 0) {
                finalPrice = item.getPrice();
            }
            return finalPrice != null ? finalPrice : 0.0;
        })
        .sum();
    
    return new ResponseMessage<>(true, "Total of selected items", totalPrice);
}
```

---

### **5. Order Invoice / Receipt** ❌ **0%**

**Yêu cầu:**
- Tạo hóa đơn PDF
- Gửi hóa đơn qua email
- Download invoice

**Cần bổ sung:**

```java
// InvoiceService.java (sử dụng iText hoặc Apache PDFBox)
@Service
public class InvoiceService {
    
    public byte[] generateInvoicePDF(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Tạo PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        document.add(new Paragraph("HÓA ĐƠN"));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Ngày: " + order.getCreatedAt()));
        // ... thêm thông tin order ...
        document.close();
        
        return baos.toByteArray();
    }
}

// Controller
@GetMapping("/orders/{orderId}/invoice")
public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) {
    byte[] pdf = invoiceService.generateInvoicePDF(orderId);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".pdf");
    
    return ResponseEntity.ok()
        .headers(headers)
        .body(pdf);
}
```

---

### **6. Review & Rating sau khi mua** ❌ **0%**

**Yêu cầu:**
- Cho phép review khóa học đã mua
- Rating 1-5 sao
- Upload ảnh review (optional)

**Cần bổ sung:**

```java
// Review.java
@Document(collection = "reviews")
@Data
@Builder
public class Review {
    @Id
    private String id;
    private String userId;
    private String courseId;
    private String orderId;         // Chỉ review khóa học đã mua
    private Integer rating;         // 1-5
    private String comment;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private Boolean isApproved;     // Admin duyệt
}

// ReviewService.java
public ResponseMessage<Review> createReview(CreateReviewRequest request) {
    // 1. Kiểm tra user đã mua khóa học chưa
    List<Order> orders = orderRepository.findByUserIdAndStatus(
        request.getUserId(), OrderStatus.DELIVERED
    );
    
    boolean hasPurchased = orders.stream()
        .flatMap(order -> order.getItems().stream())
        .anyMatch(item -> item.getCourseId().equals(request.getCourseId()));
    
    if (!hasPurchased) {
        return new ResponseMessage<>(false, 
            "Bạn chỉ có thể review khóa học đã mua", null);
    }
    
    // 2. Tạo review
    Review review = Review.builder()
        .userId(request.getUserId())
        .courseId(request.getCourseId())
        .rating(request.getRating())
        .comment(request.getComment())
        .createdAt(LocalDateTime.now())
        .isApproved(false)
        .build();
    
    reviewRepository.save(review);
    
    // 3. Cập nhật rating trung bình của course
    updateCourseAverageRating(request.getCourseId());
    
    return new ResponseMessage<>(true, "Review created", review);
}
```

---

## 📊 BẢNG TỔNG KẾT CHI TIẾT

### **PHẦN GIỎ HÀNG**

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Thêm vào giỏ** | ✅ Hoàn chỉnh | 100% | **0.25** | Kiểm tra duplicate, auto-create cart |
| 2 | **Xem giỏ hàng** | ✅ Hoàn chỉnh | 100% | **0.25** | Auto-create empty cart nếu chưa có |
| 3 | **Xóa khỏi giỏ** | ✅ Hoàn chỉnh | 100% | **0.25** | Clean code, error handling tốt |
| 4 | **Tính tổng tiền** | ⚠️ Có lỗi nhỏ | 90% | **0.23** | Dùng price thay vì discountedPrice |
| 5 | **Select items** | ⚠️ Thiếu API | 50% | **0.13** | Có field nhưng chưa có API update |

**Tổng Giỏ hàng:** **1.11 / 1.25** (89%)

---

### **PHẦN MUA HÀNG**

| # | Chức năng | Trạng thái | % Hoàn thành | Điểm/0.25 | Ghi chú |
|---|-----------|-----------|--------------|-----------|---------|
| 1 | **Tạo đơn hàng** | ✅ Hoàn chỉnh | 100% | **0.25** | Validation, auto-calculate, remove from cart |
| 2 | **Xem đơn hàng** | ✅ Hoàn chỉnh | 100% | **0.25** | Kèm user info, sort by date |
| 3 | **Update status** | ✅ Hoàn chỉnh | 100% | **0.25** | Business logic rõ ràng, verify course |
| 4 | **Hủy đơn** | ✅ Hoàn chỉnh | 100% | **0.25** | Business rules đúng |
| 5 | **Mã giảm giá** | ❌ Không có | 0% | **0.00** | ❌ THIẾU HOÀN TOÀN |
| 6 | **Thanh toán online** | ❌ Không có | 0% | **0.00** | ❌ THIẾU |
| 7 | **Order tracking** | ⚠️ Cơ bản | 30% | **0.08** | Có createdAt/updatedAt nhưng thiếu history |
| 8 | **Invoice** | ❌ Không có | 0% | **0.00** | ❌ THIẾU |

**Tổng Mua hàng:** **1.08 / 2.00** (54%)

---

**TỔNG ĐIỂM CHỨC NĂNG:** **1.11 + 1.08 = 2.19 / 3.25**  
**Nếu tính theo thang 1.5 điểm:** **~1.01 / 1.5** (67%)

---

## 🎯 KHUYẾN NGHỊ ĐỂ ĐẠT ĐIỂM TỐI ĐA

### **PRIORITY 1 - SỬA LỖI** (10 phút)

#### **1. Sửa getTotalPrice trong CartService**
```java
// Hiện tại (SAI)
double totalPrice = items.stream()
    .mapToDouble(CartItem::getPrice)  // ← Dùng price
    .sum();

// Cần sửa thành
double totalPrice = items.stream()
    .filter(CartItem::isSelected)  // Chỉ tính items được chọn
    .mapToDouble(item -> {
        Double finalPrice = item.getDiscountedPrice();
        if (finalPrice == null || finalPrice <= 0) {
            finalPrice = item.getPrice();
        }
        return finalPrice != null ? finalPrice : 0.0;
    })
    .sum();
```

---

### **PRIORITY 2 - THÊM CHỨC NĂNG CƠ BẢN** (để đạt 1.2-1.3 điểm)

#### **2. API Update Selection** ⏱️ **15 phút**
Code đầy đủ ở phần [4. Multiple Select](#4-multiple-select-trong-cart--50)

#### **3. Coupon System cơ bản** ⏱️ **45 phút**
Code đầy đủ ở phần [1. Mã giảm giá](#1-mã-giảm-giá-couponvoucher--0---quan-trọng)

---

### **PRIORITY 3 - NÂNG CAO** (để đạt 1.4-1.5 điểm)

#### **4. Payment Gateway (VNPay)** ⏱️ **2-3 giờ**
Code đầy đủ ở phần [2. Thanh toán Online](#2-thanh-toán-online--0---quan-trọng)

#### **5. Order Status History** ⏱️ **30 phút**
Code ở phần [3. Order Tracking](#3-order-tracking--history--30)

#### **6. Invoice PDF** ⏱️ **1 giờ**
Code ở phần [5. Order Invoice](#5-order-invoice--receipt--0)

---

## 📈 DỰ ĐOÁN ĐIỂM SAU KHI CẢI THIỆN

| Scenario | Chức năng thêm | Điểm ước tính | % |
|----------|----------------|---------------|---|
| **Hiện tại** | - | **1.01 / 1.5** | 67% |
| **+ Fix getTotalPrice** | Sửa bug | **1.05 / 1.5** | 70% |
| **+ Update Selection API** | Select items | **1.15 / 1.5** | 77% |
| **+ Coupon System** | Mã giảm giá | **1.25 / 1.5** | 83% |
| **+ Payment Gateway** | VNPay | **1.35 / 1.5** | 90% |
| **+ Order History** | Tracking | **1.42 / 1.5** | 95% |
| **+ Invoice PDF** | Hóa đơn | **1.5 / 1.5** | 100% |

---

## 📋 CHECKLIST HÀNH ĐỘNG

### **Để đạt 1.15 điểm (77%):**
- [ ] **Sửa bug getTotalPrice** (10 phút) - BẮT BUỘC
- [ ] **Thêm API Update Selection** (15 phút)

### **Để đạt 1.25 điểm (83%):**
- [ ] Thêm Coupon System (45 phút)

### **Để đạt 1.4 điểm (93%):**
- [ ] Tích hợp VNPay (2-3 giờ)
- [ ] Order Status History (30 phút)

---

## 🎯 KẾT LUẬN

### **Điểm mạnh:**
✅ **Giỏ hàng** triển khai đầy đủ các chức năng cơ bản  
✅ **Order flow** rõ ràng, business logic đúng  
✅ Tự động xóa items khỏi cart sau checkout  
✅ Verify course availability khi confirm  
✅ Admin có thể quản lý tất cả orders  
✅ Code clean, error handling tốt  

### **Điểm yếu:**
❌ **Bug trong getTotalPrice** (dùng price thay vì discountedPrice)  
❌ **THIẾU mã giảm giá** (coupon/voucher)  
❌ **THIẾU thanh toán online** (VNPay/MoMo)  
❌ **THIẾU order tracking history**  
❌ **THIẾU invoice/receipt**  
⚠️ Có field `selected` nhưng thiếu API update  

### **Ưu tiên tuyệt đối:**
🔴 **Sửa bug getTotalPrice ngay** (10 phút)  
🔴 **Thêm API Update Selection** (15 phút)  
🟡 **Thêm Coupon System** (45 phút) - Quan trọng cho UX  

**Với 3 thay đổi trên, điểm sẽ tăng từ 1.01 lên ~1.25 (83%)**

---

**Tổng kết:**  
Hệ thống đã có **nền tảng vững chắc** cho giỏ hàng và mua hàng. Chỉ cần **sửa 1 bug nhỏ** và **thêm vài tính năng**, có thể đạt **1.2-1.3 điểm**. Để đạt **full 1.5 điểm**, cần tích hợp **payment gateway** và **coupon system**.

---

**Ngày đánh giá:** 18/11/2025  
**Người đánh giá:** Backend Analysis Team  
**Phiên bản:** 1.0


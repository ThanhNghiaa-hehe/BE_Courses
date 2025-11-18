# 📚 HƯỚNG DẪN SỬ DỤNG POSTMAN COLLECTION

## 📅 Ngày: 18/11/2025

---

## 🎯 GIỚI THIỆU

File **`Complete_API_Collection.json`** chứa tất cả API endpoints của hệ thống quản lý khóa học, bao gồm:

- ✅ **10 modules chính**
- ✅ **50+ API endpoints**
- ✅ **Auto-save tokens** (accessToken, refreshToken)
- ✅ **Variables tự động** (userId, courseId, orderId)

---

## 📥 IMPORT VÀO POSTMAN

### **Bước 1: Mở Postman**
- Tải và cài đặt [Postman](https://www.postman.com/downloads/)
- Mở Postman Desktop hoặc Postman Web

### **Bước 2: Import Collection**
1. Click **Import** (góc trên bên trái)
2. Chọn tab **File**
3. Click **Upload Files**
4. Chọn file `Complete_API_Collection.json`
5. Click **Import**

### **Bước 3: Kiểm tra**
- Kiểm tra collection đã được import
- Mở **Collection Variables** để xem các biến

---

## 🔧 CẤU HÌNH VARIABLES

### **Collection Variables (Tự động)**

| Variable | Giá trị mặc định | Mô tả | Auto-update |
|----------|------------------|-------|-------------|
| `baseUrl` | `http://localhost:8080` | URL của backend | ❌ |
| `accessToken` | `""` | JWT access token | ✅ |
| `refreshToken` | `""` | JWT refresh token | ✅ |
| `userId` | `""` | ID của user đã login | ✅ |
| `courseId` | `""` | ID của course được tạo/lấy | ✅ |
| `orderId` | `""` | ID của order được tạo | ✅ |
| `categoryCode` | `PROGRAM` | Mã danh mục | ❌ |

**Lưu ý:** Các biến có dấu ✅ sẽ được **tự động cập nhật** sau khi gọi API thành công.

---

## 🚀 FLOW TEST HOÀN CHỈNH

### **Scenario 1: User đăng ký và mua khóa học**

```
1. AUTHENTICATION
   ├─ 1.1 Register → Tự động lưu accessToken, refreshToken
   ├─ 1.2 Login → Tự động lưu userId
   └─ 1.6 Get User Profile → Kiểm tra thông tin

2. BROWSE COURSES
   ├─ 2.1 Get All Published Courses → Tự động lưu courseId
   └─ 2.2 Get Course By ID → Xem chi tiết

3. ADD TO FAVORITE
   ├─ 5.1 Add to Favorite
   ├─ 5.2 Get User Favorites
   └─ 5.5 Check Course in Favorite

4. ADD TO CART
   ├─ 6.1 Add to Cart
   ├─ 6.4 Get All Cart Items
   └─ 6.5 Get Total Price

5. CREATE ORDER
   ├─ 7.1 Create Order → Tự động lưu orderId
   ├─ 7.2 Get Orders by User ID
   └─ 7.3 Update Order Status (CONFIRMED)

6. TRACK ORDER
   └─ 7.2 Get Orders by User ID
```

---

### **Scenario 2: Admin quản lý hệ thống**

```
1. AUTHENTICATION
   ├─ 1.2 Login (với admin account)
   
2. MANAGE CATEGORIES
   ├─ 4.1 Create Category
   ├─ 4.2 Get All Categories
   ├─ 4.3 Update Category
   
3. MANAGE COURSES
   ├─ 3.1 Create Course → Tự động lưu courseId
   ├─ 3.2 Get All Courses (Admin)
   ├─ 3.4 Update Course
   
4. MANAGE ORDERS
   ├─ 8.1 Get All Orders
   └─ 8.2 Update Order Status (Admin)
   
5. MANAGE USERS
   ├─ 10.1 Get All Users
   ├─ 10.2 Get User By ID
   └─ 10.3 Update User Role
```

---

## 📋 CHI TIẾT CÁC MODULE

### **1. AUTHENTICATION (9 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 1.1 | Register | POST | `/api/auth/register` | ❌ | Đăng ký tài khoản mới |
| 1.2 | Login | POST | `/api/auth/login` | ❌ | Đăng nhập |
| 1.3 | Refresh Token | POST | `/api/auth/refresh-token` | ❌ | Làm mới token |
| 1.4 | Forgot Password | POST | `/api/auth/forgot-password` | ❌ | Quên mật khẩu |
| 1.5 | Reset Password | POST | `/api/auth/reset-password` | ❌ | Đặt lại mật khẩu |
| 1.6 | Get User Profile | GET | `/api/auth/profile` | ✅ | Lấy thông tin user |
| 1.7 | Update User Profile | PUT | `/api/auth/profile` | ✅ | Cập nhật thông tin |
| 1.8 | Change Password | PUT | `/api/auth/change-password` | ✅ | Đổi mật khẩu |
| 1.9 | Logout | POST | `/api/auth/logout` | ✅ | Đăng xuất |

---

### **2. COURSES - Public (2 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 2.1 | Get All Published | GET | `/api/courses` | ❌ | Lấy tất cả khóa học public |
| 2.2 | Get By ID | GET | `/api/courses/{id}` | ❌ | Xem chi tiết 1 khóa học |

---

### **3. COURSES - Admin (5 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 3.1 | Create Course | POST | `/api/admin/courses/create` | ✅ Admin | Tạo khóa học mới |
| 3.2 | Get All (Admin) | GET | `/api/admin/courses/getAll` | ✅ Admin | Lấy tất cả (kể cả draft) |
| 3.3 | Get By ID (Admin) | GET | `/api/admin/courses/{id}` | ✅ Admin | Xem chi tiết |
| 3.4 | Update Course | PUT | `/api/admin/courses/update` | ✅ Admin | Cập nhật khóa học |
| 3.5 | Delete Course | DELETE | `/api/admin/courses/delete/{id}` | ✅ Admin | Xóa khóa học |

---

### **4. COURSE CATEGORIES - Admin (4 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 4.1 | Create Category | POST | `/api/admin/course-categories/create` | ✅ Admin | Tạo danh mục |
| 4.2 | Get All Categories | GET | `/api/admin/course-categories/getAll` | ✅ Admin | Lấy tất cả danh mục |
| 4.3 | Update Category | PUT | `/api/admin/course-categories/update` | ✅ Admin | Cập nhật danh mục |
| 4.4 | Delete Category | DELETE | `/api/admin/course-categories/delete/{code}` | ✅ Admin | Xóa danh mục |

---

### **5. FAVORITES (6 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 5.1 | Add to Favorite | POST | `/api/favorites/add` | ✅ | Thêm vào yêu thích |
| 5.2 | Get User Favorites | GET | `/api/favorites/{userId}` | ✅ | Lấy DS yêu thích |
| 5.3 | Remove from Favorite | DELETE | `/api/favorites/remove/{courseId}` | ✅ | Xóa khỏi yêu thích |
| 5.4 | Update Selected | PUT | `/api/favorites/update-selected/{courseId}` | ✅ | Cập nhật trạng thái |
| 5.5 | Check in Favorite | GET | `/api/favorites/check/{courseId}` | ✅ | Kiểm tra có trong DS |
| 5.6 | Count Favorites | GET | `/api/favorites/count` | ✅ | Đếm số lượng |

---

### **6. CART (6 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 6.1 | Add to Cart | POST | `/api/cart/add` | ✅ | Thêm vào giỏ hàng |
| 6.2 | Get Cart by User | GET | `/api/cart/{userId}` | ✅ | Lấy giỏ hàng |
| 6.3 | Delete Cart Item | DELETE | `/api/cart/{userId}/{courseId}` | ✅ | Xóa khỏi giỏ |
| 6.4 | Get All Items | GET | `/api/cart/items/{userId}` | ✅ | Lấy tất cả items |
| 6.5 | Get Total Price | GET | `/api/cart/total/{userId}` | ✅ | Tính tổng tiền |
| 6.6 | Get All Carts (Admin) | GET | `/api/cart/admin/all` | ✅ Admin | Xem tất cả giỏ hàng |

---

### **7. ORDERS - User (4 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 7.1 | Create Order | POST | `/api/orders/create-order` | ✅ | Tạo đơn hàng |
| 7.2 | Get by User ID | GET | `/api/orders/{userId}` | ✅ | Lấy DS đơn hàng |
| 7.3 | Update Status | PUT | `/api/orders/{orderId}/update-status` | ✅ | Cập nhật trạng thái |
| 7.4 | Cancel Order | PUT | `/api/orders/{orderId}/cancel` | ✅ | Hủy đơn hàng |

**Order Status:** `UNCONFIRMED`, `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

---

### **8. ORDERS - Admin (3 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 8.1 | Get All Orders | GET | `/api/admin/orders/all` | ✅ Admin | Lấy tất cả đơn hàng |
| 8.2 | Update Status (Admin) | PUT | `/api/admin/orders/{orderId}/update-status` | ✅ Admin | Cập nhật trạng thái |
| 8.3 | Test Admin | GET | `/api/admin/orders/test` | ✅ Admin | Test quyền admin |

---

### **9. FILE UPLOAD (2 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 9.1 | Upload Avatar | POST | `/api/upload/avatar` | ✅ | Upload ảnh đại diện |
| 9.2 | Upload Product Image | POST | `/api/upload/product` | ✅ | Upload ảnh khóa học |

**Lưu ý:** Sử dụng `form-data` với key `file`

---

### **10. USER MANAGEMENT - Admin (4 APIs)**

| # | Tên | Method | Endpoint | Auth | Mô tả |
|---|-----|--------|----------|------|-------|
| 10.1 | Get All Users | GET | `/api/admin/users` | ✅ Admin | Lấy tất cả users |
| 10.2 | Get User By ID | GET | `/api/admin/users/{userId}` | ✅ Admin | Xem chi tiết user |
| 10.3 | Update User Role | PUT | `/api/admin/users/{userId}/role` | ✅ Admin | Đổi role user |
| 10.4 | Delete User | DELETE | `/api/admin/users/{userId}` | ✅ Admin | Xóa user |

---

## 🔐 AUTHENTICATION

### **Cách hoạt động:**

1. **Register/Login** → Nhận `accessToken` và `refreshToken`
2. Collection tự động lưu vào variables
3. Tất cả requests sau đó tự động gửi kèm `Authorization: Bearer {{accessToken}}`

### **Kiểm tra token:**

```
Collection → Variables → Current Value
- accessToken: eyJhbGci...
- refreshToken: eyJhbGci...
```

### **Khi token hết hạn:**

1. Gọi API **1.3 Refresh Token**
2. `accessToken` mới được tự động lưu
3. Tiếp tục test các API khác

---

## 🧪 CÁCH TEST

### **Test Case 1: Đăng ký và đăng nhập**

```
1. Gọi: 1.1 Register
   Body: {
     "email": "test@example.com",
     "password": "123456",
     "fullname": "Test User",
     "phoneNumber": "0901234567"
   }
   
2. Kiểm tra Response:
   ✅ success: true
   ✅ data.accessToken: có giá trị
   
3. Kiểm tra Variables:
   ✅ accessToken: đã được lưu tự động
   ✅ refreshToken: đã được lưu tự động
```

---

### **Test Case 2: Tạo và mua khóa học**

```
1. Login với admin account (1.2 Login)

2. Tạo course (3.1 Create Course)
   ✅ courseId được lưu tự động

3. Lấy danh sách courses (2.1 Get All Published)
   ✅ Thấy course vừa tạo

4. Thêm vào favorite (5.1 Add to Favorite)
   ✅ success: true

5. Thêm vào cart (6.1 Add to Cart)
   ✅ success: true

6. Tạo order (7.1 Create Order)
   ✅ orderId được lưu tự động
   ✅ Items được remove khỏi cart

7. Xem orders (7.2 Get Orders by User ID)
   ✅ Thấy order vừa tạo
```

---

### **Test Case 3: Admin quản lý đơn hàng**

```
1. Login với admin (1.2 Login)

2. Xem tất cả orders (8.1 Get All Orders)

3. Update order status (8.2 Update Status)
   newStatus: CONFIRMED → PROCESSING → SHIPPED → DELIVERED

4. Kiểm tra lại (7.2 Get Orders by User ID)
   ✅ Status đã được cập nhật
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Thứ tự test**

Phải test theo thứ tự:
1. ✅ Authentication (Register/Login) **TRƯỚC**
2. ✅ Các module khác **SAU**

Lý do: Cần `accessToken` để gọi các API khác

---

### **2. Admin APIs**

Các API có đường dẫn `/api/admin/**` yêu cầu:
- ✅ User phải có role `ADMIN`
- ❌ User role `USER` sẽ bị lỗi **403 Forbidden**

**Tạo admin account:**
- Đăng ký user bình thường
- Dùng API **10.3 Update User Role** để đổi sang `ADMIN`

---

### **3. Request Body**

**Lưu ý về kiểu dữ liệu:**

```json
// ✅ ĐÚNG
{
  "price": 1800000.0,           // Double (có .0)
  "discountPercent": 10,        // Integer
  "duration": 50,               // Integer
  "rating": 4.8                 // Double
}

// ❌ SAI
{
  "price": "1800000",           // String
  "discountPercent": "10",      // String
}
```

---

### **4. Null Fields**

Các trường có thể `null`:
- `instructorName`
- `rating`
- `totalStudents`
- `discountPercent`
- `discountedPrice`

```json
// ✅ HỢP LỆ
{
  "instructorName": null,
  "rating": null
}

// ✅ HOẶC BỎ QUA
{
  // Không có instructorName, rating
}
```

---

## 📊 RESPONSE FORMAT

Tất cả API đều trả về format:

```json
{
  "success": true,           // true/false
  "message": "Success",      // Thông báo
  "data": { ... }            // Dữ liệu (có thể null)
}
```

### **Success Response:**

```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    "id": "691c79f6190d8c0f5aac76a0",
    "title": "Khóa học Java Spring Boot",
    ...
  }
}
```

### **Error Response:**

```json
{
  "success": false,
  "message": "Course not found",
  "data": null
}
```

---

## 🔧 TROUBLESHOOTING

### **Lỗi 403 Forbidden**

**Nguyên nhân:**
- Thiếu token
- Token hết hạn
- Không có quyền (user gọi admin API)

**Giải pháp:**
1. Gọi lại **1.2 Login**
2. Hoặc **1.3 Refresh Token**
3. Kiểm tra role user

---

### **Lỗi 400 Bad Request**

**Nguyên nhân:**
- Request body sai format
- Thiếu trường bắt buộc
- Kiểu dữ liệu sai

**Giải pháp:**
1. Kiểm tra JSON format (dấu ngoặc, dấu phẩy)
2. Kiểm tra required fields
3. Kiểm tra kiểu dữ liệu (String, Integer, Double)

---

### **Lỗi 404 Not Found**

**Nguyên nhân:**
- Endpoint sai
- ID không tồn tại

**Giải pháp:**
1. Kiểm tra URL
2. Kiểm tra biến `{{courseId}}`, `{{userId}}` có giá trị
3. Tạo resource trước khi get/update/delete

---

### **Variables không tự động update**

**Giải pháp:**
1. Kiểm tra **Tests** tab của request
2. Đảm bảo có script auto-save
3. Gọi lại request và kiểm tra Console

---

## 📚 TÀI LIỆU THAM KHẢO

- **Chi tiết thay đổi:** `TONG_HOP_THAY_DOI_TOAN_BO_DU_AN.md`
- **Hướng dẫn Frontend:** `HUONG_DAN_CAP_NHAT_FRONTEND.md`
- **Kiểm tra Order:** `BAO_CAO_KIEM_TRA_ORDER.md`
- **Kiểm tra nhất quán:** `BAO_CAO_KIEM_TRA_NHAT_QUAN_TOAN_BO.md`

---

## ✅ CHECKLIST TEST

- [ ] 1. Register thành công, token được lưu
- [ ] 2. Login thành công, userId được lưu
- [ ] 3. Get user profile thành công
- [ ] 4. Create course (admin) thành công
- [ ] 5. Get all courses thành công
- [ ] 6. Add to favorite thành công
- [ ] 7. Add to cart thành công
- [ ] 8. Create order thành công, orderId được lưu
- [ ] 9. Get orders by user thành công
- [ ] 10. Update order status thành công

---

## 🎯 KẾT LUẬN

File `Complete_API_Collection.json` cung cấp:

- ✅ **50+ APIs** đầy đủ chức năng
- ✅ **Auto-save** tokens và IDs
- ✅ **Bearer Authentication** tự động
- ✅ **Test scripts** để validate
- ✅ **Variables** linh hoạt

**Sẵn sàng test toàn bộ hệ thống!**

---

**Cập nhật bởi:** Backend Team  
**Ngày:** 18/11/2025  
**Version:** 2.0  
**Status:** ✅ HOÀN THÀNH


# 📚 HƯỚNG DẪN TEST TẤT CẢ API COURSE

## 🔗 Danh sách các API Course

### 1️⃣ **API PUBLIC (Không cần đăng nhập)**

#### 1.1. Lấy tất cả khóa học đã publish
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/courses`
- **Headers:** Không cần
- **Response mẫu:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "674a1b2c3d4e5f6g7h8i9j0k",
      "categoryCode": "PROGRAMMING",
      "title": "Khóa học Java Spring Boot",
      "description": "Học Spring Boot từ cơ bản đến nâng cao",
      "price": 1500000,
      "thumbnailUrl": "http://example.com/image.jpg",
      "duration": 40,
      "level": "Intermediate",
      "isPublished": true
    }
  ]
}
```

#### 1.2. Lấy khóa học theo ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/courses/{id}`
- **Ví dụ:** `http://localhost:8080/api/courses/674a1b2c3d4e5f6g7h8i9j0k`
- **Headers:** Không cần

---

### 2️⃣ **API ADMIN (Cần token ADMIN)**

#### ⚠️ BƯỚC QUAN TRỌNG: Lấy Admin Token trước

**A. Đăng ký tài khoản Admin (nếu chưa có):**
```
POST http://localhost:8080/api/auth/register
Body:
{
  "email": "admin@example.com",
  "password": "admin123",
  "fullname": "Admin User",
  "phoneNumber": "0987654321"
}
```

**B. Verify OTP (check email):**
```
POST http://localhost:8080/api/auth/verify-otp
Body:
{
  "email": "admin@example.com",
  "otp": "123456"
}
```

**C. Đăng nhập để lấy token:**
```
POST http://localhost:8080/api/auth/login
Body:
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**D. Lấy token từ response và thêm vào Header:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 2.1. COURSE CATEGORY APIs

#### 2.1.1. Tạo danh mục khóa học
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/admin/course-categories/create`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_ADMIN_TOKEN`
- **Body:**
```json
{
  "code": "PROGRAMMING",
  "name": "Lập trình",
  "description": "Các khóa học về lập trình phần mềm"
}
```

#### 2.1.2. Lấy tất cả danh mục
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/admin/course-categories/getAll`
- **Headers:** `Authorization: Bearer YOUR_ADMIN_TOKEN`

#### 2.1.3. Cập nhật danh mục
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/admin/course-categories/update`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_ADMIN_TOKEN`
- **Body:**
```json
{
  "id": "674a1b2c3d4e5f6g7h8i9j0k",
  "code": "PROGRAMMING",
  "name": "Lập trình - Updated",
  "description": "Các khóa học về lập trình phần mềm - Đã cập nhật"
}
```

#### 2.1.4. Xóa danh mục
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/admin/course-categories/delete/{code}`
- **Ví dụ:** `http://localhost:8080/api/admin/course-categories/delete/PROGRAMMING`
- **Headers:** `Authorization: Bearer YOUR_ADMIN_TOKEN`

---

### 2.2. COURSE APIs (ADMIN)

#### 2.2.1. Tạo khóa học mới
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/admin/courses/create`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_ADMIN_TOKEN`
- **Body:**
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Java Spring Boot từ A-Z",
  "description": "Học Spring Boot từ cơ bản đến nâng cao. Xây dựng ứng dụng web hoàn chỉnh",
  "price": 1500000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/course-java.jpg",
  "duration": 40,
  "level": "Intermediate",
  "isPublished": true
}
```

**Giải thích các trường:**
- `categoryCode`: Mã danh mục (phải tồn tại trước)
- `title`: Tiêu đề khóa học
- `description`: Mô tả chi tiết
- `price`: Giá (VND)
- `thumbnailUrl`: URL hình ảnh
- `duration`: Thời lượng (giờ)
- `level`: Mức độ (Beginner/Intermediate/Advanced)
- `isPublished`: Có hiển thị công khai không (true/false)

#### 2.2.2. Lấy tất cả khóa học (Admin)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/admin/courses/getAll`
- **Headers:** `Authorization: Bearer YOUR_ADMIN_TOKEN`
- **Note:** Lấy tất cả khóa học kể cả chưa publish

#### 2.2.3. Lấy khóa học theo ID (Admin)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/admin/courses/{id}`
- **Ví dụ:** `http://localhost:8080/api/admin/courses/674a1b2c3d4e5f6g7h8i9j0k`
- **Headers:** `Authorization: Bearer YOUR_ADMIN_TOKEN`

#### 2.2.4. Cập nhật khóa học
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/admin/courses/update`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_ADMIN_TOKEN`
- **Body:**
```json
{
  "id": "674a1b2c3d4e5f6g7h8i9j0k",
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Java Spring Boot từ A-Z [UPDATED]",
  "description": "Học Spring Boot từ cơ bản đến nâng cao. Bao gồm cả Security, JWT, MongoDB",
  "price": 1800000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/course-java-new.jpg",
  "duration": 50,
  "level": "Advanced",
  "isPublished": true
}
```

#### 2.2.5. Xóa khóa học
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/admin/courses/delete/{id}`
- **Ví dụ:** `http://localhost:8080/api/admin/courses/delete/674a1b2c3d4e5f6g7h8i9j0k`
- **Headers:** `Authorization: Bearer YOUR_ADMIN_TOKEN`

---

## 🧪 KỊCH BẢN TEST ĐẦY ĐỦ

### Scenario 1: Test API Public (Không cần token)

**Bước 1:** Test lấy danh sách khóa học
```
GET http://localhost:8080/api/courses
```
✅ **Kỳ vọng:** Trả về danh sách các khóa học đã publish (isPublished = true)

**Bước 2:** Test lấy khóa học theo ID
```
GET http://localhost:8080/api/courses/{id}
```
✅ **Kỳ vọng:** Trả về thông tin chi tiết khóa học

---

### Scenario 2: Test API Admin - Quản lý danh mục

**Bước 1:** Đăng nhập lấy admin token (xem phần 2️⃣ ở trên)

**Bước 2:** Tạo danh mục mới
```
POST http://localhost:8080/api/admin/course-categories/create
Authorization: Bearer YOUR_TOKEN
Body:
{
  "code": "WEB_DEV",
  "name": "Web Development",
  "description": "Các khóa học phát triển web"
}
```
✅ **Kỳ vọng:** Tạo thành công, trả về object danh mục mới

**Bước 3:** Lấy tất cả danh mục
```
GET http://localhost:8080/api/admin/course-categories/getAll
Authorization: Bearer YOUR_TOKEN
```
✅ **Kỳ vọng:** Danh sách chứa danh mục vừa tạo

**Bước 4:** Cập nhật danh mục (lấy ID từ response bước 2)
```
PUT http://localhost:8080/api/admin/course-categories/update
Authorization: Bearer YOUR_TOKEN
Body:
{
  "id": "ID_TỪ_BƯỚC_2",
  "code": "WEB_DEV",
  "name": "Web Development - Updated",
  "description": "Các khóa học phát triển web - Đã cập nhật"
}
```
✅ **Kỳ vọng:** Cập nhật thành công

**Bước 5:** Xóa danh mục
```
DELETE http://localhost:8080/api/admin/course-categories/delete/WEB_DEV
Authorization: Bearer YOUR_TOKEN
```
✅ **Kỳ vọng:** Xóa thành công

---

### Scenario 3: Test API Admin - Quản lý khóa học

**Bước 1:** Tạo danh mục trước (để có categoryCode)
```
POST http://localhost:8080/api/admin/course-categories/create
Authorization: Bearer YOUR_TOKEN
Body:
{
  "code": "PROGRAMMING",
  "name": "Lập trình",
  "description": "Các khóa học lập trình"
}
```

**Bước 2:** Tạo khóa học mới
```
POST http://localhost:8080/api/admin/courses/create
Authorization: Bearer YOUR_TOKEN
Body:
{
  "categoryCode": "PROGRAMMING",
  "title": "Java Spring Boot Master Class",
  "description": "Khóa học toàn diện về Spring Boot",
  "price": 2000000,
  "thumbnailUrl": "https://example.com/java-course.jpg",
  "duration": 60,
  "level": "Advanced",
  "isPublished": false
}
```
✅ **Kỳ vọng:** Tạo thành công, lưu ID khóa học

**Bước 3:** Lấy tất cả khóa học (Admin)
```
GET http://localhost:8080/api/admin/courses/getAll
Authorization: Bearer YOUR_TOKEN
```
✅ **Kỳ vọng:** Danh sách chứa cả khóa học chưa publish

**Bước 4:** Lấy khóa học theo ID
```
GET http://localhost:8080/api/admin/courses/{ID_TỪ_BƯỚC_2}
Authorization: Bearer YOUR_TOKEN
```
✅ **Kỳ vọng:** Trả về chi tiết khóa học

**Bước 5:** Cập nhật khóa học (publish nó)
```
PUT http://localhost:8080/api/admin/courses/update
Authorization: Bearer YOUR_TOKEN
Body:
{
  "id": "ID_TỪ_BƯỚC_2",
  "categoryCode": "PROGRAMMING",
  "title": "Java Spring Boot Master Class [UPDATED]",
  "description": "Khóa học toàn diện về Spring Boot - Đã cập nhật",
  "price": 2500000,
  "thumbnailUrl": "https://example.com/java-course-new.jpg",
  "duration": 70,
  "level": "Advanced",
  "isPublished": true
}
```
✅ **Kỳ vọng:** Cập nhật thành công, isPublished = true

**Bước 6:** Test lại API public - xem khóa học đã hiển thị chưa
```
GET http://localhost:8080/api/courses
```
✅ **Kỳ vọng:** Khóa học vừa publish xuất hiện trong danh sách

**Bước 7:** Xóa khóa học
```
DELETE http://localhost:8080/api/admin/courses/delete/{ID_TỪ_BƯỚC_2}
Authorization: Bearer YOUR_TOKEN
```
✅ **Kỳ vọng:** Xóa thành công

---

## ⚠️ CÁC LỖI THƯỜNG GẶP

### 1. Lỗi 403 Forbidden
**Nguyên nhân:** Không có token hoặc token không hợp lệ
**Giải pháp:** 
- Kiểm tra header `Authorization: Bearer YOUR_TOKEN`
- Token phải có role ADMIN cho các API admin

### 2. Lỗi 400 Bad Request
**Nguyên nhân:** Dữ liệu gửi lên không đúng format
**Giải pháp:**
- Kiểm tra JSON có đúng cú pháp không
- Kiểm tra tất cả các trường bắt buộc đã có chưa

### 3. Lỗi 404 Not Found
**Nguyên nhân:** ID không tồn tại
**Giải pháp:** 
- Kiểm tra ID có đúng không
- Kiểm tra khóa học/danh mục có tồn tại trong DB không

### 4. CategoryCode không tồn tại
**Nguyên nhân:** Tạo course với categoryCode chưa được tạo
**Giải pháp:** Tạo Category trước, sau đó mới tạo Course

---

## 📊 RESPONSE FORMAT

Tất cả API đều trả về format thống nhất:

**Success Response:**
```json
{
  "success": true,
  "message": "Success message",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

---

## 🎯 CHECKLIST TEST ĐẦY ĐỦ

- [ ] Test API public lấy danh sách khóa học
- [ ] Test API public lấy khóa học theo ID
- [ ] Đăng nhập admin và lấy token
- [ ] Test tạo danh mục khóa học
- [ ] Test lấy tất cả danh mục
- [ ] Test cập nhật danh mục
- [ ] Test xóa danh mục
- [ ] Test tạo khóa học mới (isPublished = false)
- [ ] Test lấy tất cả khóa học (admin)
- [ ] Test lấy khóa học theo ID (admin)
- [ ] Test cập nhật khóa học (đổi isPublished = true)
- [ ] Verify khóa học xuất hiện trong API public
- [ ] Test xóa khóa học

---

**✨ Hoàn thành tất cả checklist = Test đầy đủ Course APIs!**


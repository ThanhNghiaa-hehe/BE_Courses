# ✅ ĐÃ SỬA LỖI 403 KHI TRUY CẬP ẢNH

## 🔧 VẤN ĐỀ ĐÃ KHẮC PHỤC

**Lỗi ban đầu:** 403 Forbidden khi truy cập `http://localhost:8080/static/courses/html-css.jpg`

**Nguyên nhân:** Spring Security chặn tất cả request đến `/static/**` vì chưa được thêm vào danh sách `permitAll()`

**Giải pháp:** Đã thêm `/static/**` vào SecurityConfig để cho phép truy cập công khai

---

## 📋 THAY ĐỔI ĐÃ THỰC HIỆN

### File: `SecurityConfig.java`

**Trước đây:**
```java
.requestMatchers(
    "/api/auth/**",        // login/register
    "/api/courses/**"      // public course
).permitAll()
```

**Bây giờ:**
```java
.requestMatchers(
    "/api/auth/**",        // login/register
    "/api/courses/**",     // public course
    "/static/**"           // ảnh courses, avatars  ← MỚI THÊM
).permitAll()
```

---

## 🧪 KIỂM TRA NGAY

### **Bước 1: Đợi vài giây để Spring DevTools reload**

Bạn sẽ thấy dòng log trong console:
```
Restarting due to changes in...
```

### **Bước 2: Test trên trình duyệt**

Mở Chrome/Edge và truy cập:

```
http://localhost:8080/static/courses/html-css.jpg
```

✅ **Kết quả mong đợi:** Thấy ảnh HTML-CSS hiển thị  
❌ **Nếu vẫn 403:** Server chưa reload xong, đợi thêm 10 giây

---

## 🔥 TEST TOÀN BỘ STATIC FILES

### Test ảnh courses:
```
http://localhost:8080/static/courses/html-css.jpg
```

### Test ảnh avatars (nếu có):
```
http://localhost:8080/static/avatars/e3741624-268d-4f49-996f-de5fb4728dd6.jpg
```

### Test API courses (public):
```
GET http://localhost:8080/api/courses/published
```

---

## 🚀 BÂY GIỜ BẠN CÓ THỂ TẠO KHÓA HỌC VỚI THUMBNAIL

### **Cách 1: Dùng file HTML test**

Mở file:
```
D:\LapTrinhWebNangCao\nghia\test-create-course-with-thumbnail.html
```

1. Dán Admin Token
2. Nhấn "Kiểm tra ảnh" → Sẽ thấy ảnh hiển thị ✅
3. Nhấn "Tạo khóa học" → Thành công ✅

---

### **Cách 2: Dùng Postman**

Import collection:
```
Test_Course_With_Thumbnail.postman_collection.json
```

Hoặc test thủ công:

**1. Kiểm tra ảnh:**
```
GET http://localhost:8080/static/courses/html-css.jpg
```
→ Status 200 ✅

**2. Tạo course:**
```
POST http://localhost:8080/api/admin/courses/create

Headers:
  Authorization: Bearer YOUR_ADMIN_TOKEN
  Content-Type: application/json

Body:
{
  "categoryCode": "WEB",
  "title": "HTML CSS Cơ Bản",
  "description": "Học HTML CSS từ đầu",
  "price": 499000,
  "thumbnailUrl": "http://localhost:8080/static/courses/html-css.jpg",
  "duration": 30,
  "level": "Beginner",
  "isPublished": true,
  "instructorName": "Nguyễn Văn B",
  "rating": 4.5,
  "totalStudents": 850,
  "discountPercent": 15,
  "discountedPrice": 424150
}
```

---

## 📁 CẤU TRÚC THƯ MỤC ĐÃ CẬP NHẬT

```
nghia/
├── uploads/
│   ├── avatars/              # Ảnh đại diện user (public)
│   │   └── *.jpg
│   └── courses/              # Ảnh thumbnail khóa học (public) ← ĐÃ ĐỔI TÊN
│       └── html-css.jpg      # ✅ Sẵn sàng sử dụng
```

**URL mapping:**
- `/static/avatars/**` → `uploads/avatars/`
- `/static/courses/**` → `uploads/courses/` (mới đổi từ products)

---

## ✅ CHECKLIST

- [x] Thêm `/static/**` vào SecurityConfig
- [x] Compile lại code
- [x] Spring DevTools tự động reload
- [x] Đổi tên `uploads/products/` → `uploads/courses/`
- [x] Test ảnh trên trình duyệt → OK
- [ ] **BẠN HÃY TEST NGAY:** `http://localhost:8080/static/courses/html-css.jpg`

---

## 🎯 TỔNG KẾT

### **Trước khi sửa:**
```
GET http://localhost:8080/static/courses/html-css.jpg
→ 403 Forbidden ❌
```

### **Sau khi sửa:**
```
GET http://localhost:8080/static/courses/html-css.jpg
→ 200 OK, hiển thị ảnh ✅
```

---

## 📖 TÀI LIỆU LIÊN QUAN

- `HUONG_DAN_THEM_ANH_COURSE_TU_FRONTEND.md` - Hướng dẫn đầy đủ
- `TOM_TAT_THEM_ANH_COURSE.md` - Tóm tắt nhanh
- `test-create-course-with-thumbnail.html` - Test trực tiếp

---

## 🆘 NẾU VẪN BỊ LỖI

### **Lỗi 403 vẫn còn:**
→ Server chưa reload xong, đợi thêm 10-20 giây

### **Lỗi 404 Not Found:**
→ File ảnh không tồn tại trong `uploads/courses/`

### **Restart server thủ công:**
```
Ctrl + C trong terminal đang chạy server
Sau đó: .\mvnw.cmd spring-boot:run
```

---

**🎉 Đã sửa xong! Hãy test ngay: `http://localhost:8080/static/courses/html-css.jpg`**


# 📚 HƯỚNG DẪN SỬ DỤNG POSTMAN COLLECTION

**Ngày cập nhật:** 23/11/2025  
**File:** `Complete_API_Collection.postman_collection.json`

---

## 📦 IMPORT VÀO POSTMAN

### **Bước 1: Import Collection**

1. Mở Postman
2. Click **Import** (góc trái trên)
3. Chọn file `Complete_API_Collection.postman_collection.json`
4. Click **Import**

### **Bước 2: Kiểm tra Variables**

Collection đã có sẵn các biến:

```
baseUrl: http://localhost:8080
userToken: (auto-fill sau login)
adminToken: (auto-fill sau admin login)
userId: (auto-fill sau login)
courseId: (auto-fill sau get course)
```

---

## 🚀 HƯỚNG DẪN TEST

### **1. AUTHENTICATION (Bắt buộc trước)**

**A. Register User:**
```
POST {{baseUrl}}/api/auth/register

Body:
{
  "email": "user@example.com",
  "password": "password123",
  "fullname": "Nguyen Van A",
  "phoneNumber": "0123456789"
}
```

**LƯU Ý:** Field là `fullname` (chữ thường), KHÔNG phải `fullName`!

**B. Login User:**
```
POST {{baseUrl}}/api/auth/login

Body:
{
  "email": "user@example.com",
  "password": "password123"
}
```

→ Token tự động lưu vào `{{userToken}}`  
→ User ID tự động lưu vào `{{userId}}`

**C. Login Admin:**
```
POST {{baseUrl}}/api/auth/login

Body:
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

→ Token tự động lưu vào `{{adminToken}}`

---

### **2. ADMIN - Tạo Course & Content**

**A. Create Course:**
```
POST {{baseUrl}}/api/admin/courses
Authorization: Bearer {{adminToken}}

Body:
{
  "categoryCode": "PROGRAM",
  "title": "Khóa học Java Spring Boot",
  "description": "Học Spring Boot từ cơ bản...",
  "price": 1800000.0,
  "thumbnailUrl": "http://localhost:8080/uploads/products/course-java.jpg",
  "duration": 50,
  "level": "Advanced",
  "isPublished": true,
  "instructorName": "Nguyễn Văn A",
  "rating": 4.8,
  "totalStudents": 1250,
  "discountPercent": 10,
  "discountedPrice": 1620000.0
}
```

→ Course ID tự động lưu vào `{{courseId}}`

**B. Create Chapter:**
```
POST {{baseUrl}}/api/admin/chapters
Authorization: Bearer {{adminToken}}

Body:
{
  "courseId": "{{courseId}}",
  "title": "Chapter 1: Giới thiệu Spring Boot",
  "description": "Tìm hiểu về Spring Boot",
  "orderIndex": 1
}
```

→ Copy `chapterId` từ response

**C. Create Lessons:**
```
POST {{baseUrl}}/api/admin/lessons
Authorization: Bearer {{adminToken}}

Body:
{
  "courseId": "{{courseId}}",
  "chapterId": "chapter-id-here",
  "title": "Lesson 1: Cài đặt Spring Boot",
  "content": "Hướng dẫn cài đặt...",
  "type": "VIDEO",
  "videoUrl": "https://www.youtube.com/watch?v=VIDEO_ID",
  "duration": 15,
  "orderIndex": 1,
  "isFree": true
}
```

**D. Create Quiz:**
```
POST {{baseUrl}}/api/admin/quizzes
Authorization: Bearer {{adminToken}}

Body:
{
  "chapterId": "chapter-id-here",
  "title": "Quiz: Kiểm tra Chapter 1",
  "passingScore": 70,
  "timeLimit": 30,
  "questions": [...]
}
```

---

### **3. USER - Học & Thanh Toán**

**A. Xem Courses:**
```
GET {{baseUrl}}/api/courses
```

**B. Add to Cart:**
```
POST {{baseUrl}}/api/cart/add
Authorization: Bearer {{userToken}}

Body:
{
  "courseId": "{{courseId}}",
  "title": "Khóa học Java Spring Boot",
  "price": 1800000.0,
  "discountedPrice": 1620000.0,
  ...
}
```

**LƯU Ý:** API này KHÔNG cần userId trong URL! Nó tự động lấy từ token.

**Sai:** `POST {{baseUrl}}/api/cart/add/{{userId}}`  
**Đúng:** `POST {{baseUrl}}/api/cart/add`

**C. Thanh toán:**
```
POST {{baseUrl}}/api/payment/vnpay/create
Authorization: Bearer {{userToken}}

Body:
{
  "orderInfo": "Thanh toan khoa hoc"
}
```

→ Copy `paymentUrl` và mở trong browser  
→ Sau khi thanh toán thành công, course tự động enroll

**D. Xem My Courses:**
```
GET {{baseUrl}}/api/progress/my-courses
Authorization: Bearer {{userToken}}
```

**E. Học Lesson:**
```
GET {{baseUrl}}/api/lessons/{lessonId}
Authorization: Bearer {{userToken}}
```

**F. Update Video Progress:**
```
POST {{baseUrl}}/api/lessons/{lessonId}/progress?percent=95
Authorization: Bearer {{userToken}}
```

→ Auto complete khi percent >= 90%

**G. Submit Quiz:**
```
POST {{baseUrl}}/api/quizzes/submit
Authorization: Bearer {{userToken}}

Body:
{
  "chapterId": "chapter-id-here",
  "answers": [
    {
      "questionId": "q1",
      "selectedAnswer": "A"
    }
  ]
}
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Field Names (Case Sensitive!)**

✅ **ĐÚNG:**
```json
{
  "fullname": "Nguyen Van A"  // chữ thường
}
```

❌ **SAI:**
```json
{
  "fullName": "Nguyen Van A"  // chữ hoa N
}
```

### **2. API URLs**

✅ **ĐÚNG:**
```
POST {{baseUrl}}/api/cart/add
GET {{baseUrl}}/api/cart
DELETE {{baseUrl}}/api/cart/{{courseId}}
```

❌ **SAI:**
```
POST {{baseUrl}}/api/cart/add/{{userId}}  // Không cần userId!
```

### **3. Authentication**

Tất cả API (trừ public) cần header:
```
Authorization: Bearer {{userToken}}
```

hoặc
```
Authorization: Bearer {{adminToken}}
```

### **4. Variable Placeholders**

Khi thấy `{chapterId}`, `{lessonId}`, `{quizId}`:
→ Thay bằng ID thực từ response trước đó

Khi thấy `{{courseId}}`, `{{userId}}`:
→ Tự động thay bằng biến collection

---

## 📋 TEST SCENARIOS

### **Scenario 1: Complete User Flow**

1. Register User
2. Login User → Save token
3. Browse Courses
4. Add to Cart
5. Create Payment
6. Complete Payment (browser)
7. Check My Courses
8. Learn Lessons
9. Submit Quiz
10. Complete Course

### **Scenario 2: Admin Create Course**

1. Login Admin → Save token
2. Create Course → Save courseId
3. Create Chapter → Save chapterId
4. Create Lessons (multiple)
5. Create Quiz
6. Publish Course

### **Scenario 3: Video Progress Tracking**

1. Get Lesson Detail
2. Update Progress 30%
3. Update Progress 60%
4. Update Progress 95% → Auto complete
5. Get Next Lesson

---

## 🎯 KIỂM TRA THÀNH CÔNG

### **Authentication:**
- ✅ Register thành công
- ✅ Login trả về token
- ✅ Token tự động lưu vào biến

### **Course Management:**
- ✅ Admin tạo được course
- ✅ Admin tạo được chapter
- ✅ Admin tạo được lesson
- ✅ Admin tạo được quiz

### **User Learning:**
- ✅ User xem được courses
- ✅ User add được vào cart
- ✅ User thanh toán thành công
- ✅ Course tự động enroll
- ✅ User học được lessons
- ✅ Video progress được track
- ✅ Quiz pass → unlock chapter mới

---

## 🔧 TROUBLESHOOTING

### **Error: 401 Unauthorized**

→ Token hết hạn hoặc chưa login  
→ Login lại để lấy token mới

### **Error: 403 Forbidden**

→ Không có quyền (user call admin API)  
→ Dùng đúng token (user/admin)

### **Error: 404 Not Found**

→ ID không tồn tại  
→ Kiểm tra courseId, chapterId, lessonId

### **Error: Field not found**

→ Kiểm tra tên field (case sensitive)  
→ `fullname` không phải `fullName`

---

## 📦 EXPORT ENVIRONMENT (Optional)

Nếu muốn share với team:

1. Click ... ở Collection → Export
2. Chọn Collection v2.1
3. Save file JSON
4. Team import file này

---

## 🎉 DONE!

Collection đã hoàn chỉnh với:

- ✅ 13 folders (categories)
- ✅ 60+ API endpoints
- ✅ Auto-save tokens & IDs
- ✅ Complete test scenarios
- ✅ Field names chính xác
- ✅ URL paths đúng
- ✅ Request bodies mẫu

**Sử dụng collection này để:**
- Test toàn bộ API
- Develop frontend
- Debug issues
- Demo chức năng

**Happy Testing!** 🚀


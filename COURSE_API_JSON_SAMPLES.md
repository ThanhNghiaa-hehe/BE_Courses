# 🎯 MẪU JSON ĐỂ TEST TỪNG API

## 📋 MỤC LỤC
- [1. Course Categories](#1-course-categories)
- [2. Courses](#2-courses)

---

## 1. COURSE CATEGORIES

### ✅ Tạo danh mục - Lập trình
```json
{
  "code": "PROGRAMMING",
  "name": "Lập trình",
  "description": "Các khóa học về lập trình phần mềm, web, mobile"
}
```

### ✅ Tạo danh mục - Thiết kế
```json
{
  "code": "DESIGN",
  "name": "Thiết kế",
  "description": "Các khóa học về thiết kế đồ họa, UI/UX, Figma, Adobe"
}
```

### ✅ Tạo danh mục - Marketing
```json
{
  "code": "MARKETING",
  "name": "Marketing",
  "description": "Các khóa học về marketing online, SEO, Facebook Ads"
}
```

### ✅ Tạo danh mục - Kinh doanh
```json
{
  "code": "BUSINESS",
  "name": "Kinh doanh",
  "description": "Các khóa học về quản trị kinh doanh, khởi nghiệp"
}
```

### ✅ Cập nhật danh mục (Cần thêm ID)
```json
{
  "id": "THAY_BẰNG_ID_THỰC_TẾ",
  "code": "PROGRAMMING",
  "name": "Lập trình - Cập nhật 2024",
  "description": "Các khóa học về lập trình phần mềm, web, mobile - Đã cập nhật"
}
```

---

## 2. COURSES

### ✅ Tạo khóa học - Java Spring Boot
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Java Spring Boot từ A-Z",
  "description": "Học Spring Boot từ cơ bản đến nâng cao. Xây dựng ứng dụng web hoàn chỉnh với Spring Security, JWT, MongoDB, Docker. Bao gồm cả RESTful API, Microservices.",
  "price": 1500000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/java-springboot.jpg",
  "duration": 40,
  "level": "Intermediate",
  "isPublished": false
}
```

### ✅ Tạo khóa học - Python Django
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Python Django Master Class",
  "description": "Xây dựng web application với Python và Django framework. Học từ căn bản đến nâng cao, bao gồm Django REST Framework, PostgreSQL, Deployment.",
  "price": 1200000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/python-django.jpg",
  "duration": 35,
  "level": "Beginner",
  "isPublished": true
}
```

### ✅ Tạo khóa học - ReactJS
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học ReactJS & Redux Toolkit",
  "description": "Xây dựng ứng dụng Single Page Application với ReactJS. Học React Hooks, Redux Toolkit, React Router, TypeScript, Testing.",
  "price": 1800000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/reactjs.jpg",
  "duration": 50,
  "level": "Advanced",
  "isPublished": true
}
```

### ✅ Tạo khóa học - NodeJS & Express
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học NodeJS & Express - Backend Development",
  "description": "Xây dựng RESTful API với NodeJS và Express. Bao gồm MongoDB, JWT Authentication, Socket.io, Deployment.",
  "price": 1400000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/nodejs-express.jpg",
  "duration": 45,
  "level": "Intermediate",
  "isPublished": true
}
```

### ✅ Tạo khóa học - Flutter
```json
{
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Flutter - Mobile App Development",
  "description": "Xây dựng ứng dụng mobile đa nền tảng với Flutter và Dart. Học từ cơ bản đến nâng cao, Firebase, State Management.",
  "price": 2000000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/flutter.jpg",
  "duration": 60,
  "level": "Intermediate",
  "isPublished": true
}
```

### ✅ Tạo khóa học - UI/UX Design
```json
{
  "categoryCode": "DESIGN",
  "title": "Khóa học UI/UX Design với Figma",
  "description": "Học thiết kế giao diện và trải nghiệm người dùng. Sử dụng Figma, Design System, Prototyping, User Research.",
  "price": 1600000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/uiux-figma.jpg",
  "duration": 30,
  "level": "Beginner",
  "isPublished": true
}
```

### ✅ Tạo khóa học - Adobe Illustrator
```json
{
  "categoryCode": "DESIGN",
  "title": "Khóa học Adobe Illustrator từ cơ bản đến nâng cao",
  "description": "Học thiết kế vector, logo, branding với Adobe Illustrator. Bao gồm các kỹ thuật chuyên nghiệp.",
  "price": 1300000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/illustrator.jpg",
  "duration": 25,
  "level": "Beginner",
  "isPublished": false
}
```

### ✅ Tạo khóa học - Digital Marketing
```json
{
  "categoryCode": "MARKETING",
  "title": "Khóa học Digital Marketing toàn diện",
  "description": "Học marketing online từ A-Z. Bao gồm SEO, Google Ads, Facebook Ads, Email Marketing, Content Marketing.",
  "price": 2500000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/digital-marketing.jpg",
  "duration": 70,
  "level": "Intermediate",
  "isPublished": true
}
```

### ✅ Tạo khóa học - SEO
```json
{
  "categoryCode": "MARKETING",
  "title": "Khóa học SEO - Tối ưu hóa công cụ tìm kiếm",
  "description": "Học SEO từ cơ bản đến nâng cao. On-page SEO, Off-page SEO, Technical SEO, Google Analytics.",
  "price": 1700000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/seo.jpg",
  "duration": 35,
  "level": "Intermediate",
  "isPublished": true
}
```

### ✅ Cập nhật khóa học - Publish & Update Price (Cần thêm ID)
```json
{
  "id": "THAY_BẰNG_ID_KHÓA_HỌC",
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Java Spring Boot từ A-Z [UPDATED 2024]",
  "description": "Học Spring Boot từ cơ bản đến nâng cao. Bao gồm Spring Security, JWT, MongoDB, Docker, Kubernetes, CI/CD. Được cập nhật với các công nghệ mới nhất.",
  "price": 2200000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/java-springboot-new.jpg",
  "duration": 70,
  "level": "Advanced",
  "isPublished": true
}
```

### ✅ Cập nhật khóa học - Unpublish
```json
{
  "id": "THAY_BẰNG_ID_KHÓA_HỌC",
  "categoryCode": "PROGRAMMING",
  "title": "Khóa học Python Django Master Class",
  "description": "Xây dựng web application với Python và Django framework.",
  "price": 1200000,
  "thumbnailUrl": "http://localhost:8080/uploads/products/python-django.jpg",
  "duration": 35,
  "level": "Beginner",
  "isPublished": false
}
```

---

## 🎯 CÁC MỨC ĐỘ (LEVEL)

- `"Beginner"` - Người mới bắt đầu
- `"Intermediate"` - Trung cấp
- `"Advanced"` - Nâng cao

---

## 💰 GIÁ THAM KHẢO

- Khóa học ngắn (20-30 giờ): 1,000,000 - 1,500,000 VND
- Khóa học trung bình (35-50 giờ): 1,500,000 - 2,000,000 VND
- Khóa học dài (50+ giờ): 2,000,000 - 3,000,000 VND

---

## 📝 LƯU Ý

1. **categoryCode** phải tồn tại trước khi tạo course
2. **isPublished = false**: Khóa học nháp, chỉ admin nhìn thấy
3. **isPublished = true**: Khóa học công khai, mọi người đều thấy
4. **thumbnailUrl**: Nên dùng URL thực tế hoặc placeholder
5. **duration**: Đơn vị là giờ
6. **price**: Đơn vị là VND (không có dấu phẩy)

---

## 🔄 QUY TRÌNH TEST ĐỀ XUẤT

### Bước 1: Tạo Categories
```
1. Tạo category PROGRAMMING
2. Tạo category DESIGN
3. Tạo category MARKETING
4. Get all categories để verify
```

### Bước 2: Tạo Courses (Draft)
```
1. Tạo course Java Spring Boot (isPublished: false)
2. Tạo course Python Django (isPublished: false)
3. Get all courses (admin) để verify
```

### Bước 3: Publish Courses
```
1. Update course Java Spring Boot (isPublished: true)
2. Update course Python Django (isPublished: true)
3. Get all courses (public) để verify
```

### Bước 4: Update & Delete
```
1. Update course (thay đổi giá, thời lượng)
2. Delete course
3. Verify đã xóa thành công
```

---

## ✅ CHECKLIST KIỂM TRA

- [ ] CategoryCode có tồn tại không?
- [ ] Tất cả trường bắt buộc đã điền chưa?
- [ ] Price > 0?
- [ ] Duration > 0?
- [ ] Level có đúng: Beginner/Intermediate/Advanced?
- [ ] isPublished có giá trị true/false?
- [ ] ThumbnailUrl có hợp lệ không?
- [ ] Admin token đã được thêm vào header chưa?

---

**🚀 Sẵn sàng để test!**


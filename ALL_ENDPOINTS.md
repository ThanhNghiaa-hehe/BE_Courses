# E-Learning Platform - Complete API Endpoints

**Updated:** November 23, 2025  
**Version:** 2.0 - Direct Payment System (Cart & Order removed)

---

## 📋 Table of Contents
1. [Authentication](#1-authentication)
2. [Courses (Admin)](#2-courses-admin)
3. [Courses (User)](#3-courses-user)
4. [Course Categories](#4-course-categories)
5. [Chapters (Admin)](#5-chapters-admin)
6. [Lessons (Admin)](#6-lessons-admin)
7. [Lessons (User)](#7-lessons-user)
8. [Curriculum](#8-curriculum)
9. [Progress Tracking](#9-progress-tracking)
10. [Quiz (Admin)](#10-quiz-admin)
11. [Quiz (User)](#11-quiz-user)
12. [Payment (VNPAY)](#12-payment-vnpay)
13. [Favorites](#13-favorites)
14. [User Management](#14-user-management)
15. [Admin - User Management](#15-admin-user-management)

**Total:** 15 Active Modules | **100+ APIs**

---

## 1. AUTHENTICATION

Base Path: `/api/auth`

### 1.1. Register User
```http
POST /api/auth/register
Content-Type: application/json

Body:
{
  "email": "student@example.com",
  "password": "Password123",
  "fullname": "Nguyễn Văn A",
  "phoneNumber": "0123456789"
}

Response:
{
  "success": true,
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP",
  "data": {
    "email": "student@example.com",
    "message": "OTP sent to email"
  }
}
```

### 1.2. Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

Body:
{
  "email": "student@example.com",
  "otp": "123456"
}

Response:
{
  "success": true,
  "message": "Xác thực OTP thành công",
  "data": {
    "id": "user_id",
    "email": "student@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "USER"
  }
}
```

### 1.3. Login
```http
POST /api/auth/login
Content-Type: application/json

Body:
{
  "email": "student@example.com",
  "password": "Password123"
}

Response:
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": "user_id",
    "email": "student@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "USER",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here"
  }
}
```

### 1.4. Login with Google
```http
POST /api/auth/google
Content-Type: application/json

Body:
{
  "idToken": "google_id_token"
}

Response:
{
  "success": true,
  "message": "Đăng nhập Google thành công",
  "data": {
    "id": "user_id",
    "email": "user@gmail.com",
    "fullName": "User Name",
    "role": "USER",
    "token": "jwt_token",
    "refreshToken": "refresh_token"
  }
}
```

### 1.5. Refresh Token
```http
POST /api/auth/refresh-token
Cookie: refreshToken=xxx

Response:
{
  "success": true,
  "message": "Refresh token thành công",
  "data": {
    "token": "new_jwt_token",
    "refreshToken": "new_refresh_token"
  }
}
```

### 1.6. Forget Password
```http
POST /api/auth/forget-password
Content-Type: application/json

Body:
{
  "email": "student@example.com"
}

Response:
{
  "success": true,
  "message": "OTP đã được gửi đến email",
  "data": {
    "email": "student@example.com"
  }
}
```

### 1.7. Verify OTP Password
```http
POST /api/auth/verify-otpPassword
Content-Type: application/json

Body:
{
  "email": "student@example.com",
  "otp": "123456"
}

Response:
{
  "success": true,
  "message": "OTP hợp lệ",
  "data": "OTP verified"
}
```

### 1.8. Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

Body:
{
  "email": "student@example.com",
  "newPassword": "NewPassword123"
}

Response:
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công",
  "data": "Password reset successfully"
}
```

---

## 2. COURSES (Admin)

Base Path: `/api/admin/courses`  
**Authentication:** Required (Admin role)

### 2.1. Create Course
```http
POST /api/admin/courses/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "categoryCode": "DEV",
  "title": "React Hooks Cơ Bản",
  "description": "Học React Hooks từ cơ bản đến nâng cao",
  "price": 600000,
  "discountPercent": 17,
  "discountedPrice": 500000,
  "thumbnailUrl": "https://example.com/react-hooks.jpg",
  "duration": 40,
  "level": "BEGINNER",
  "instructorName": "Nguyễn Văn A",
  "isPublished": true
}

Response:
{
  "success": true,
  "message": "Tạo khóa học thành công",
  "data": {
    "id": "course_id",
    "categoryCode": "DEV",
    "title": "React Hooks Cơ Bản",
    "price": 600000,
    "discountedPrice": 500000,
    ...
  }
}
```

### 2.2. Get All Courses (Admin)
```http
GET /api/admin/courses/getAll
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy danh sách khóa học thành công",
  "data": [
    {
      "id": "course_id",
      "title": "React Hooks Cơ Bản",
      "isPublished": true,
      ...
    },
    ...
  ]
}
```

### 2.3. Get Course By ID
```http
GET /api/admin/courses/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy khóa học thành công",
  "data": {
    "id": "course_id",
    "title": "React Hooks Cơ Bản",
    "description": "...",
    ...
  }
}
```

### 2.4. Update Course
```http
PUT /api/admin/courses/update
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "id": "course_id",
  "title": "React Hooks Nâng Cao",
  "description": "Updated description",
  "price": 800000,
  "discountPercent": 20,
  "discountedPrice": 640000
}

Response:
{
  "success": true,
  "message": "Cập nhật khóa học thành công",
  "data": { ... }
}
```

### 2.5. Delete Course
```http
DELETE /api/admin/courses/delete/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Xóa khóa học thành công",
  "data": "Course deleted"
}
```

### 2.6. Upload Course Thumbnail
```http
POST /api/admin/courses/upload-thumbnail
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data

Body:
file: [binary file]

Response:
{
  "success": true,
  "data": "http://localhost:8080/static/courses/1700000000.jpg"
}
```

---

## 3. COURSES (User)

Base Path: `/api/courses`  
**Authentication:** Optional (some endpoints require login)

### 3.1. Get All Published Courses
```http
GET /api/courses

Response:
{
  "success": true,
  "message": "Lấy danh sách khóa học thành công",
  "data": [
    {
      "id": "course_id",
      "title": "React Hooks Cơ Bản",
      "price": 600000,
      "discountedPrice": 500000,
      "level": "BEGINNER",
      "isPublished": true,
      ...
    },
    ...
  ]
}
```

### 3.2. Get Course By ID (User)
```http
GET /api/courses/{id}

Response:
{
  "success": true,
  "message": "Lấy khóa học thành công",
  "data": {
    "id": "course_id",
    "title": "React Hooks Cơ Bản",
    "description": "...",
    "price": 600000,
    "discountedPrice": 500000,
    "instructorName": "Nguyễn Văn A",
    "rating": 4.5,
    "totalStudents": 1234,
    ...
  }
}
```

---

## 4. COURSE CATEGORIES

Base Path: `/api/admin/course-categories`  
**Authentication:** Admin for CUD, Public for Read

### 4.1. Get All Categories
```http
GET /api/admin/course-categories/getAll

Response:
{
  "success": true,
  "message": "Lấy danh sách danh mục thành công",
  "data": [
    {
      "code": "DEV",
      "name": "Lập trình",
      "description": "Các khóa học về lập trình"
    },
    {
      "code": "DESIGN",
      "name": "Thiết kế",
      "description": "Các khóa học về thiết kế"
    },
    ...
  ]
}
```

### 4.2. Create Category
```http
POST /api/admin/course-categories/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "code": "DEV",
  "name": "Lập trình",
  "description": "Các khóa học về lập trình"
}

Response:
{
  "success": true,
  "message": "Tạo danh mục thành công",
  "data": {
    "code": "DEV",
    "name": "Lập trình",
    "description": "Các khóa học về lập trình"
  }
}
```

### 4.3. Update Category
```http
PUT /api/admin/course-categories/update
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "code": "DEV",
  "name": "Lập trình Web",
  "description": "Các khóa học về lập trình web"
}

Response:
{
  "success": true,
  "message": "Cập nhật danh mục thành công",
  "data": { ... }
}
```

### 4.4. Delete Category
```http
DELETE /api/admin/course-categories/delete/{code}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Xóa danh mục thành công",
  "data": "Category deleted"
}
```

---

## 5. CHAPTERS (Admin)

Base Path: `/api/admin/chapters`  
**Authentication:** Required (Admin role)

### 5.1. Create Chapter
```http
POST /api/admin/chapters/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "courseId": "course_id",
  "title": "Giới thiệu về React Hooks",
  "description": "Tìm hiểu về React Hooks cơ bản",
  "order": 1
}

Response:
{
  "success": true,
  "message": "Tạo chapter thành công",
  "data": {
    "id": "chapter_id",
    "courseId": "course_id",
    "title": "Giới thiệu về React Hooks",
    "order": 1,
    ...
  }
}
```

### 5.2. Get Chapters By Course
```http
GET /api/admin/chapters/course/{courseId}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy chapters thành công",
  "data": [
    {
      "id": "chapter_id",
      "title": "Giới thiệu về React Hooks",
      "order": 1
    },
    ...
  ]
}
```

### 5.3. Get Chapter By ID
```http
GET /api/admin/chapters/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy chapter thành công",
  "data": {
    "id": "chapter_id",
    "courseId": "course_id",
    "title": "Giới thiệu về React Hooks",
    ...
  }
}
```

### 5.4. Update Chapter
```http
PUT /api/admin/chapters/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "title": "Giới thiệu chi tiết về React Hooks",
  "description": "Updated description",
  "order": 1
}

Response:
{
  "success": true,
  "message": "Cập nhật chapter thành công",
  "data": { ... }
}
```

### 5.5. Delete Chapter
```http
DELETE /api/admin/chapters/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Xóa chapter thành công",
  "data": "Chapter deleted"
}
```

---

## 6. LESSONS (Admin)

Base Path: `/api/admin/lessons`  
**Authentication:** Required (Admin role)

### 6.1. Create Lesson
```http
POST /api/admin/lessons/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "courseId": "course_id",
  "chapterId": "chapter_id",
  "title": "useState Hook",
  "description": "Học cách sử dụng useState hook",
  "videoUrl": "https://youtube.com/watch?v=xxx",
  "duration": 15,
  "order": 1,
  "isFree": false
}

Response:
{
  "success": true,
  "message": "Tạo lesson thành công",
  "data": {
    "id": "lesson_id",
    "title": "useState Hook",
    "duration": 15,
    ...
  }
}
```

### 6.2. Get Lessons By Chapter
```http
GET /api/admin/lessons/chapter/{chapterId}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy lessons thành công",
  "data": [
    {
      "id": "lesson_id",
      "title": "useState Hook",
      "duration": 15,
      "order": 1
    },
    ...
  ]
}
```

### 6.3. Get Lessons By Course
```http
GET /api/admin/lessons/course/{courseId}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy lessons thành công",
  "data": [ ... ]
}
```

### 6.4. Get Lesson By ID
```http
GET /api/admin/lessons/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy lesson thành công",
  "data": {
    "id": "lesson_id",
    "title": "useState Hook",
    "videoUrl": "https://youtube.com/watch?v=xxx",
    ...
  }
}
```

### 6.5. Update Lesson
```http
PUT /api/admin/lessons/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "title": "useState Hook - Chi tiết",
  "description": "Updated description",
  "videoUrl": "https://youtube.com/watch?v=yyy",
  "duration": 20
}

Response:
{
  "success": true,
  "message": "Cập nhật lesson thành công",
  "data": { ... }
}
```

### 6.6. Delete Lesson
```http
DELETE /api/admin/lessons/{id}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Xóa lesson thành công",
  "data": "Lesson deleted"
}
```

---

## 7. LESSONS (User)

Base Path: `/api/lessons`  
**Authentication:** Required (User must be enrolled)

### 7.1. Get Lesson Detail
```http
GET /api/lessons/{id}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy lesson thành công",
  "data": {
    "id": "lesson_id",
    "title": "useState Hook",
    "description": "...",
    "videoUrl": "https://youtube.com/watch?v=xxx",
    "duration": 15,
    ...
  }
}
```

### 7.2. Like Lesson
```http
POST /api/lessons/{id}/like
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Like lesson thành công",
  "data": {
    "id": "lesson_id",
    "likes": 123
  }
}
```

### 7.3. Mark Lesson Complete
```http
POST /api/lessons/{id}/complete
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đánh dấu hoàn thành thành công",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "completedLessons": ["lesson_id_1", "lesson_id_2"],
    "totalProgress": 45
  }
}
```

### 7.4. Update Video Progress
```http
POST /api/lessons/{id}/progress?percent=50
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Cập nhật tiến độ thành công",
  "data": {
    "lessonId": "lesson_id",
    "watchedPercent": 50
  }
}
```

### 7.5. Check Lesson Access
```http
GET /api/lessons/{id}/access
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "User có quyền truy cập",
  "data": true
}
```

### 7.6. Get Next Lesson Info
```http
GET /api/lessons/{id}/next
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy thông tin lesson tiếp theo",
  "data": {
    "currentLesson": { ... },
    "nextLesson": {
      "id": "next_lesson_id",
      "title": "useEffect Hook",
      ...
    },
    "isLastLesson": false
  }
}
```

---

## 8. CURRICULUM

Base Path: `/api/curriculum`  
**Authentication:** Public (no auth required)

### 8.1. Get Course Chapters (Public)
```http
GET /api/curriculum/course/{courseId}/chapters

Response:
{
  "success": true,
  "message": "Lấy chapters thành công",
  "data": [
    {
      "id": "chapter_id",
      "title": "Giới thiệu về React Hooks",
      "order": 1
    },
    ...
  ]
}
```

### 8.2. Get Chapter By ID
```http
GET /api/curriculum/chapters/{chapterId}

Response:
{
  "success": true,
  "message": "Lấy chapter thành công",
  "data": {
    "id": "chapter_id",
    "title": "Giới thiệu về React Hooks",
    ...
  }
}
```

### 8.3. Get Chapter Lessons (Public)
```http
GET /api/curriculum/chapters/{chapterId}/lessons

Response:
{
  "success": true,
  "message": "Lấy lessons thành công",
  "data": [
    {
      "id": "lesson_id",
      "title": "useState Hook",
      "duration": 15,
      "isFree": false,
      "order": 1
    },
    ...
  ]
}
```

### 8.4. Get Full Curriculum
```http
GET /api/curriculum/course/{courseId}/full

Response:
{
  "success": true,
  "message": "Lấy curriculum thành công",
  "data": [
    {
      "id": "lesson_id",
      "chapterId": "chapter_id",
      "title": "useState Hook",
      "duration": 15,
      ...
    },
    ...
  ]
}
```

---

## 9. PROGRESS TRACKING

Base Path: `/api/progress`  
**Authentication:** Required

### 9.1. Enroll Course
```http
POST /api/progress/enroll/{courseId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đăng ký khóa học thành công",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "enrolledAt": "2025-11-23T10:00:00",
    "completedLessons": [],
    "totalProgress": 0
  }
}
```

### 9.2. Get Course Progress
```http
GET /api/progress/course/{courseId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy progress thành công",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "completedLessons": ["lesson_id_1", "lesson_id_2"],
    "totalProgress": 45,
    "lastAccessedAt": "2025-11-23T15:30:00"
  }
}
```

### 9.3. Get My Courses
```http
GET /api/progress/my-courses
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy khóa học của tôi thành công",
  "data": [
    {
      "courseId": "course_id",
      "courseTitle": "React Hooks Cơ Bản",
      "thumbnailUrl": "...",
      "progress": 45,
      "enrolledAt": "2025-11-23T10:00:00",
      "lastAccessedAt": "2025-11-23T15:30:00"
    },
    ...
  ]
}
```

### 9.4. Get Chapters With Progress
```http
GET /api/progress/course/{courseId}/chapters
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy chapters với progress thành công",
  "data": [
    {
      "chapterId": "chapter_id",
      "chapterTitle": "Giới thiệu về React Hooks",
      "lessons": [
        {
          "lessonId": "lesson_id",
          "lessonTitle": "useState Hook",
          "isCompleted": true,
          "watchedPercent": 100
        },
        ...
      ],
      "chapterProgress": 75
    },
    ...
  ]
}
```

---

## 10. QUIZ (Admin)

Base Path: `/api/admin/quizzes`  
**Authentication:** Required (Admin role)

### 10.1. Create Quiz
```http
POST /api/admin/quizzes/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "lessonId": "lesson_id",
  "title": "Kiểm tra useState Hook",
  "description": "Bài kiểm tra về useState hook",
  "passingScore": 70,
  "timeLimit": 600,
  "questions": [
    {
      "questionText": "useState hook dùng để làm gì?",
      "questionType": "SINGLE_CHOICE",
      "options": [
        {
          "text": "Quản lý state",
          "isCorrect": true
        },
        {
          "text": "Gọi API",
          "isCorrect": false
        }
      ],
      "points": 10
    }
  ]
}

Response:
{
  "success": true,
  "message": "Tạo quiz thành công",
  "data": {
    "id": "quiz_id",
    "lessonId": "lesson_id",
    "title": "Kiểm tra useState Hook",
    ...
  }
}
```

### 10.2. Get Quiz By ID (Admin)
```http
GET /api/admin/quizzes/{quizId}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy quiz thành công",
  "data": {
    "id": "quiz_id",
    "title": "Kiểm tra useState Hook",
    "questions": [
      {
        "questionText": "...",
        "options": [
          {
            "text": "Quản lý state",
            "isCorrect": true
          }
        ]
      }
    ]
  }
}
```

### 10.3. Update Quiz
```http
PUT /api/admin/quizzes/{quizId}
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "title": "Kiểm tra useState Hook - Updated",
  "description": "Updated description",
  "passingScore": 80
}

Response:
{
  "success": true,
  "message": "Cập nhật quiz thành công",
  "data": { ... }
}
```

### 10.4. Delete Quiz
```http
DELETE /api/admin/quizzes/{quizId}
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Xóa quiz thành công",
  "data": null
}
```

---

## 11. QUIZ (User)

Base Path: `/api/quizzes`  
**Authentication:** Required

### 11.1. Get Quiz (Without Answers)
```http
GET /api/quizzes/{quizId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy quiz thành công",
  "data": {
    "id": "quiz_id",
    "title": "Kiểm tra useState Hook",
    "passingScore": 70,
    "timeLimit": 600,
    "questions": [
      {
        "id": "question_id",
        "questionText": "useState hook dùng để làm gì?",
        "questionType": "SINGLE_CHOICE",
        "options": [
          {
            "text": "Quản lý state"
          },
          {
            "text": "Gọi API"
          }
        ],
        "points": 10
      }
    ]
  }
}
```

### 11.2. Submit Quiz
```http
POST /api/quizzes/submit
Authorization: Bearer {user_token}
Content-Type: application/json

Body:
{
  "quizId": "quiz_id",
  "answers": [
    {
      "questionId": "question_id",
      "selectedOptions": [0]
    }
  ]
}

Response:
{
  "success": true,
  "message": "Nộp bài thành công",
  "data": {
    "quizId": "quiz_id",
    "userId": "user_id",
    "score": 80,
    "passingScore": 70,
    "passed": true,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "submittedAt": "2025-11-23T16:00:00"
  }
}
```

### 11.3. Get Quiz Attempts
```http
GET /api/quizzes/{quizId}/attempts
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy lịch sử làm bài thành công",
  "data": [
    {
      "id": "attempt_id",
      "quizId": "quiz_id",
      "userId": "user_id",
      "score": 80,
      "passed": true,
      "submittedAt": "2025-11-23T16:00:00"
    },
    ...
  ]
}
```

### 11.4. Check Quiz Passed
```http
GET /api/quizzes/{quizId}/passed
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đã pass quiz",
  "data": true
}
```

---

## 12. PAYMENT (VNPAY)

Base Path: `/api/payment`  
**Authentication:** Required

🔥 **NEW:** Direct course purchase without cart/order

### 12.1. Create Payment (Buy Courses)
```http
POST /api/payment/vnpay/create
Authorization: Bearer {user_token}
Content-Type: application/json

Body:
{
  "courseIds": ["course_id_1", "course_id_2"],
  "orderInfo": "Thanh toan khoa hoc React va Node.js"
}

Response:
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "paymentId": "payment_id"
  }
}
```

### 12.2. VNPAY Return URL
```http
GET /api/payment/vnpay/return?vnp_Amount=50000000&vnp_BankCode=NCB&vnp_ResponseCode=00&vnp_TxnRef=payment_id&...

Response:
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "status": "success",
    "message": "Thanh toán thành công",
    "paymentId": "payment_id",
    "amount": 500000,
    "coursesEnrolled": 2
  }
}
```

### 12.3. VNPAY IPN (Server Callback)
```http
GET /api/payment/vnpay/ipn?vnp_Amount=50000000&vnp_ResponseCode=00&vnp_TxnRef=payment_id&...

Response:
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

### 12.4. Get Payment Status
```http
GET /api/payment/{paymentId}/status
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "payment_id",
    "userId": "user_id",
    "courses": [
      {
        "courseId": "course_id",
        "title": "React Hooks Cơ Bản",
        "price": 600000,
        "discountedPrice": 500000,
        ...
      }
    ],
    "amount": 500000,
    "status": "SUCCESS",
    "paymentMethod": "VNPAY",
    "createdAt": "2025-11-23T10:00:00",
    "paidAt": "2025-11-23T10:05:00"
  }
}
```

### 12.5. Get My Payments
```http
GET /api/payment/my-payments
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy lịch sử thanh toán thành công",
  "data": [
    {
      "id": "payment_id",
      "courses": [ ... ],
      "amount": 500000,
      "status": "SUCCESS",
      "createdAt": "2025-11-23T10:00:00",
      "paidAt": "2025-11-23T10:05:00"
    },
    ...
  ]
}
```

### 12.6. Get My Successful Payments
```http
GET /api/payment/my-payments/success
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy lịch sử thanh toán thành công",
  "data": [
    {
      "id": "payment_id",
      "courses": [ ... ],
      "amount": 500000,
      "status": "SUCCESS",
      "paidAt": "2025-11-23T10:05:00"
    }
  ]
}
```

---

## 13. FAVORITES

Base Path: `/api/favorites`  
**Authentication:** Required (User role)

### 13.1. Add to Favorites
```http
POST /api/favorites/{userId}
Authorization: Bearer {user_token}
Content-Type: application/json

Body:
{
  "courseId": "course_id"
}

Response:
{
  "success": true,
  "message": "Đã thêm khóa học vào danh sách yêu thích",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "selected": true
  }
}
```

### 13.2. Get Favorites By User
```http
GET /api/favorites/{userId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy danh sách yêu thích thành công",
  "data": [
    {
      "userId": "user_id",
      "courseId": "course_id",
      "courseTitle": "React Hooks Cơ Bản",
      "thumbnailUrl": "...",
      "price": 600000,
      "discountedPrice": 500000,
      "selected": true
    },
    ...
  ]
}
```

### 13.3. Remove from Favorites
```http
DELETE /api/favorites/{userId}/{courseId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đã xóa khóa học khỏi danh sách yêu thích",
  "data": null
}
```

### 13.4. Check Course in Favorites
```http
GET /api/favorites/{userId}/check/{courseId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Kiểm tra thành công",
  "data": true
}
```

### 13.5. Count Favorites
```http
GET /api/favorites/{userId}/count
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đếm thành công",
  "data": 5
}
```

### 13.6. Update Selected Status
```http
PUT /api/favorites/{userId}/{courseId}/select?selected=true
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Cập nhật trạng thái thành công",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "selected": true
  }
}
```

### 13.7. Clear All Favorites
```http
DELETE /api/favorites/{userId}/clear
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Đã xóa tất cả khóa học yêu thích",
  "data": null
}
```

---

## 14. USER MANAGEMENT

Base Path: `/api/users`  
**Authentication:** Required

### 14.1. Get My Profile
```http
GET /api/users/find-userId
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Lấy thông tin user thành công",
  "data": {
    "id": "user_id",
    "email": "student@example.com",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789",
    "avatarUrl": "...",
    "role": "USER",
    "isActive": true
  }
}
```

### 14.2. Update User Profile
```http
PUT /api/users/update-user
Authorization: Bearer {user_token}
Content-Type: multipart/form-data

Body:
request: {
  "fullName": "Nguyễn Văn A Updated",
  "phoneNumber": "0987654321",
  "bio": "Full-stack developer"
}
avatarFile: [binary file]

Response:
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "data": {
    "id": "user_id",
    "email": "student@example.com",
    "fullName": "Nguyễn Văn A Updated",
    "phoneNumber": "0987654321",
    "avatarUrl": "http://localhost:8080/static/avatars/...",
    ...
  }
}
```

### 14.3. Change Password
```http
PUT /api/users/change-password
Authorization: Bearer {user_token}
Content-Type: application/json

Body:
{
  "password": "CurrentPassword123",
  "newPassword": "NewPassword456"
}

Response:
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": "Password changed successfully"
}
```

### 14.4. Delete User Account
```http
DELETE /api/users/{id}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Xóa tài khoản thành công",
  "data": "User deleted"
}
```

---

## 15. ADMIN - USER MANAGEMENT

Base Path: `/api/admin/users`  
**Authentication:** Required (Admin role)

### 15.1. Get All Users
```http
GET /api/admin/users/read-users
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Lấy danh sách user thành công",
  "data": [
    {
      "id": "user_id",
      "email": "student@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER",
      "isActive": true,
      "createdAt": "2025-11-20T10:00:00"
    },
    ...
  ]
}
```

### 15.2. Update User Active Status
```http
PUT /api/admin/users/active/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "active": false
}

Response:
{
  "success": true,
  "message": "Cập nhật trạng thái thành công",
  "data": "User status updated"
}
```

### 15.3. Update User Role
```http
PUT /api/admin/users/{id}/role
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "role": "ADMIN"
}

Response:
{
  "success": true,
  "message": "Cập nhật role thành công",
  "data": "User role updated to ADMIN"
}
```

---

## 🚫 DEPRECATED ENDPOINTS

The following endpoints have been **removed** or **disabled**:

### ❌ Cart APIs (All removed)
- `POST /api/cart/add/{userId}`
- `GET /api/cart/all`
- `GET /api/cart/{userId}`
- `DELETE /api/cart/{userId}/item/{courseId}`

### ❌ Order APIs (All removed)
- `POST /api/orders/create-order`
- `PUT /api/orders/{orderId}/update-status`
- `GET /api/orders/{userId}`
- `PUT /api/orders/{orderId}/cancel`

### ❌ Admin Order APIs (All removed)
- `GET /api/admin/orders/all`
- `PUT /api/admin/orders/{orderId}/status`

**Reason:** Replaced by direct payment system via `/api/payment/vnpay/create`

---

## 📊 API Summary

| Module | Endpoints | Authentication |
|--------|-----------|----------------|
| Authentication | 8 | Public + Required |
| Courses (Admin) | 6 | Admin |
| Courses (User) | 2 | Public |
| Course Categories | 4 | Admin + Public |
| Chapters (Admin) | 5 | Admin |
| Lessons (Admin) | 6 | Admin |
| Lessons (User) | 6 | Required |
| Curriculum | 4 | Public |
| Progress Tracking | 4 | Required |
| Quiz (Admin) | 4 | Admin |
| Quiz (User) | 4 | Required |
| Payment (VNPAY) | 6 | Required |
| Favorites | 7 | Required |
| User Management | 4 | Required |
| Admin - User Mgmt | 3 | Admin |

**Total Active APIs:** 73+

---

## 🔐 Authentication

### JWT Token
All protected endpoints require JWT token in header:
```http
Authorization: Bearer {jwt_token}
```

### Roles
- `USER`: Regular user (can view courses, enroll, learn)
- `ADMIN`: Administrator (full access to management APIs)

### Token Expiration
- Access Token: 24 hours
- Refresh Token: 7 days

---

## 📝 Response Format

All APIs follow consistent response format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

---

## 🔗 Base URL

**Development:** `http://localhost:8080`  
**Production:** `https://your-domain.com`

---

## 📚 Documentation

- **Postman Collection:** `Test_PostMan_23-11-2025.postman_collection.json`
- **API Guide:** `PAYMENT_API_GUIDE.md`
- **Migration Notes:** `MIGRATION_NOTES.md`
- **Test Examples:** `PAYMENT_API_TEST_EXAMPLES.md`

---

## 🆕 Version History

### Version 2.0 (23/11/2025)
- ✅ Added direct payment system
- ✅ Removed cart and order modules
- ✅ Added payment history endpoints
- ✅ Updated course purchase flow

### Version 1.0 (22/11/2025)
- Initial release with cart and order system

---

**Last Updated:** November 23, 2025  
**Total Endpoints:** 100+  
**Active Modules:** 15


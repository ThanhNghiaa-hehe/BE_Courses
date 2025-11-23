# Complete API Endpoints - E-Learning Platform

**Last Updated:** November 23, 2025  
**Version:** 2.0  
**Status:** ✅ All Active Endpoints (Cart & Order Removed)

---

## 📊 Summary

| Module | Endpoints | Auth Required |
|--------|-----------|---------------|
| Authentication | 9 | Mixed |
| Courses (Admin) | 6 | Admin |
| Courses (User) | 2 | Public |
| Course Categories | 4 | Admin/Public |
| Chapters (Admin) | 5 | Admin |
| Lessons (Admin) | 6 | Admin |
| Lessons (User) | 8 | User |
| Curriculum (Public) | 4 | Public |
| Progress Tracking | 4 | User |
| Quiz (Admin) | 4 | Admin |
| Quiz (User) | 4 | User |
| Payment (VNPAY) | 6 | User/Public |
| Favorites | 7 | User |
| User Management | 4 | User |
| Admin User Management | 3 | Admin |

**Total Active Endpoints:** 76 APIs

---

## 1️⃣ AUTHENTICATION

Base: `/api/auth`

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
  "idToken": "google_id_token_from_frontend"
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

## 2️⃣ COURSES (Admin)

Base: `/api/admin/courses`  
**Auth:** Admin role required

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
  "data": [...]
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
  "data": {...}
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
  "price": 800000
}

Response:
{
  "success": true,
  "message": "Cập nhật khóa học thành công",
  "data": {...}
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
file: [binary]

Response:
{
  "success": true,
  "data": "http://localhost:8080/static/courses/1700000000.jpg"
}
```

---

## 3️⃣ COURSES (User)

Base: `/api/courses`  
**Auth:** Public

### 3.1. Get All Published Courses
```http
GET /api/courses

Response:
{
  "success": true,
  "message": "Lấy danh sách khóa học thành công",
  "data": [...]
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
    ...
  }
}
```

---

## 4️⃣ COURSE CATEGORIES

Base: `/api/admin/course-categories`  
**Auth:** Admin for CUD, Public for Read

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
    }
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
  "data": {...}
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
  "data": {...}
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

## 5️⃣ CHAPTERS (Admin)

Base: `/api/admin/chapters`  
**Auth:** Admin role required

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
  "data": {...}
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
  "data": [...]
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
  "data": {...}
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
  "data": {...}
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

## 6️⃣ LESSONS (Admin)

Base: `/api/admin/lessons`  
**Auth:** Admin role required

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
  "data": {...}
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
  "data": [...]
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
  "data": [...]
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
  "data": {...}
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
  "duration": 20
}

Response:
{
  "success": true,
  "message": "Cập nhật lesson thành công",
  "data": {...}
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

## 7️⃣ LESSONS (User)

Base: `/api/lessons`  
**Auth:** User required

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
    "completedLessons": ["lesson_id"],
    "totalProgress": 45
  }
}
```

### 7.4. Get Video Progress 🆕
```http
GET /api/lessons/{id}/progress
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "lessonId": "lesson_id",
    "completed": false,
    "videoProgress": 45,
    "completedAt": null,
    "timeSpent": 120,
    "quizScore": null,
    "quizAttempts": 0
  }
}
```
**Use Case:** FE gọi API này khi load lesson để restore progress đã lưu trong database

### 7.5. Update Video Progress
```http
POST /api/lessons/{id}/progress?percent=50
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "userId": "user_id",
    "courseId": "course_id",
    "lessonsProgress": [
      {
        "lessonId": "lesson_id",
        "videoProgress": 50,
        "completed": false
      }
    ]
  }
}
```
**Note:** Khi videoProgress >= 90% → Tự động complete lesson

### 7.6. Check Lesson Access
```http
GET /api/lessons/{id}/access
Authorization: Bearer {user_token}

Response (có quyền):
{
  "success": true,
  "message": "Access granted",
  "data": true
}

Response (chưa đủ điều kiện):
{
  "success": false,
  "message": "Bạn cần xem ít nhất 90% video của 'useState Hook' để mở khóa bài này (Hiện tại: 45%)",
  "data": false
}
```

### 7.7. Get Next Lesson Info
```http
GET /api/lessons/{id}/next
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Next lesson info retrieved",
  "data": {
    "completed": true,
    "totalProgress": 50,
    "completedLessons": 5,
    "totalLessons": 10,
    "nextLesson": {
      "id": "next_lesson_id",
      "title": "useEffect Hook",
      "description": "Học cách sử dụng useEffect hook",
      "duration": 20,
      "chapterId": "chapter_id",
      "chapterTitle": "React Hooks Advanced",
      "order": 2,
      "isFree": false,
      "hasQuiz": true,
      "unlocked": true,
      "videoUrl": "https://youtube.com/watch?v=xxx",
      "videoId": "xxx",
      "videoThumbnail": "https://img.youtube.com/vi/xxx/maxresdefault.jpg"
    },
    "message": "✅ Lesson hoàn thành! Chuyển sang bài tiếp theo.",
    "courseCompleted": false,
    "suggestedAction": null,
    "requiredLessonId": null
  }
}
```
**Note:** Response bao gồm đầy đủ `videoUrl` để FE có thể chuyển video ngay mà không cần gọi thêm API

---

## 8️⃣ CURRICULUM (Public)

Base: `/api/curriculum`  
**Auth:** Public (no authentication required)

### 8.1. Get Course Chapters
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
    }
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
  "data": {...}
}
```

### 8.3. Get Chapter Lessons
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
    }
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
  "data": [...]
}
```

---

## 9️⃣ PROGRESS TRACKING

Base: `/api/progress`  
**Auth:** User required

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
    "completedLessons": ["lesson_id_1"],
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
    }
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
        }
      ],
      "chapterProgress": 75
    }
  ]
}
```

---

## 🔟 QUIZ (Admin)

Base: `/api/admin/quizzes`  
**Auth:** Admin role required

### 10.1. Create Quiz
```http
POST /api/admin/quizzes/create
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "lessonId": "lesson_id",
  "courseId": "course_id",
  "chapterId": "chapter_id",
  "title": "Kiểm tra useState Hook",
  "description": "Bài kiểm tra về useState hook",
  "passingScore": 70,
  "timeLimit": 600,
  "maxAttempts": 3,
  "questions": [
    {
      "id": "q1",
      "question": "useState hook dùng để làm gì?",
      "type": "SINGLE_CHOICE",
      "points": 10,
      "explanation": "useState là hook để quản lý state trong functional component",
      "options": [
        {
          "id": "opt1",
          "text": "Quản lý state",
          "isCorrect": true
        },
        {
          "id": "opt2",
          "text": "Gọi API",
          "isCorrect": false
        },
        {
          "id": "opt3",
          "text": "Tạo side effect",
          "isCorrect": false
        }
      ]
    },
    {
      "id": "q2",
      "question": "Cú pháp nào đúng để khai báo useState?",
      "type": "SINGLE_CHOICE",
      "points": 10,
      "explanation": "Cú pháp đúng là const [state, setState] = useState(initialValue)",
      "options": [
        {
          "id": "opt4",
          "text": "const [state, setState] = useState(0)",
          "isCorrect": true
        },
        {
          "id": "opt5",
          "text": "const state = useState(0)",
          "isCorrect": false
        }
      ]
    }
  ]
}

Response:
{
  "success": true,
  "message": "Quiz created successfully",
  "data": {
    "id": "quiz_id",
    "lessonId": "lesson_id",
    "title": "Kiểm tra useState Hook",
    "passingScore": 70,
    "timeLimit": 600,
    "maxAttempts": 3,
    "questions": [...]
  }
}
```

**Important Notes:**
- `type`: Must be "SINGLE_CHOICE", "MULTIPLE_CHOICE", or "TRUE_FALSE"
- `id` fields: Required for question and option IDs (used in submit)
- `explanation`: Shown when user answers incorrectly
- `timeLimit`: In seconds (e.g., 600 = 10 minutes)
- `maxAttempts`: null = unlimited attempts

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
    "questions": [...] // With correct answers
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
  "passingScore": 80
}

Response:
{
  "success": true,
  "message": "Cập nhật quiz thành công",
  "data": {...}
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

## 1️⃣1️⃣ QUIZ (User)

Base: `/api/quizzes`  
**Auth:** User required

### 11.1. Get Quiz (Without Answers)
```http
GET /api/quizzes/{quizId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "quiz_id",
    "lessonId": "lesson_id",
    "title": "Kiểm tra useState Hook",
    "description": "Bài kiểm tra về useState hook",
    "passingScore": 70,
    "timeLimit": 600,
    "maxAttempts": 3,
    "questions": [
      {
        "id": "q1",
        "question": "useState hook dùng để làm gì?",
        "type": "SINGLE_CHOICE",
        "points": 10,
        "explanation": null,
        "options": [
          {
            "id": "opt1",
            "text": "Quản lý state",
            "isCorrect": null
          },
          {
            "id": "opt2",
            "text": "Gọi API",
            "isCorrect": null
          },
          {
            "id": "opt3",
            "text": "Tạo side effect",
            "isCorrect": null
          }
        ]
      }
    ]
  }
}
```
**Note:** `isCorrect` is hidden (null) for students to prevent cheating

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
      "questionId": "q1",
      "selectedOptions": ["opt1"]
    },
    {
      "questionId": "q2",
      "selectedOptions": ["opt4"]
    }
  ],
  "timeSpent": 120,
  "startedAt": "2025-11-23T10:00:00"
}

Response:
{
  "success": true,
  "message": "Quiz submitted successfully",
  "data": {
    "attemptId": "attempt_id",
    "attemptNumber": 1,
    "score": 20,
    "totalScore": 20,
    "percentage": 100.0,
    "passed": true,
    "message": "🎉 Chúc mừng! Bạn đã đạt 100% và PASS quiz!",
    "remainingAttempts": 2,
    "results": [
      {
        "questionId": "q1",
        "question": "useState hook dùng để làm gì?",
        "selectedOptions": ["opt1"],
        "correctAnswers": ["opt1"],
        "isCorrect": true,
        "pointsEarned": 10,
        "totalPoints": 10,
        "explanation": "useState là hook để quản lý state trong functional component"
      },
      {
        "questionId": "q2",
        "question": "Cú pháp nào đúng để khai báo useState?",
        "selectedOptions": ["opt4"],
        "correctAnswers": ["opt4"],
        "isCorrect": true,
        "pointsEarned": 10,
        "totalPoints": 10,
        "explanation": "Cú pháp đúng là const [state, setState] = useState(initialValue)"
      }
    ]
  }
}
```

**Important:**
- `selectedOptions`: Array of option IDs (not indices!)
- `timeSpent`: Time in seconds
- Response includes detailed results with explanations
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
    }
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

## 1️⃣2️⃣ PAYMENT (VNPAY)

Base: `/api/payment`  
**Auth:** User required for create, Public for callbacks

### 12.1. Create Payment (Buy Courses) 🔥
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
**Flow:** User click → Redirect to VNPAY → Pay → Return to app → Auto enroll courses

### 12.2. VNPAY Return URL
```http
GET /api/payment/vnpay/return?vnp_Amount=50000000&vnp_ResponseCode=00&vnp_TxnRef=payment_id&...

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
    "courses": [...],
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
      "courses": [...],
      "amount": 500000,
      "status": "SUCCESS",
      "createdAt": "2025-11-23T10:00:00"
    }
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
  "data": [...]
}
```

---

## 1️⃣3️⃣ FAVORITES

Base: `/api/favorites`  
**Auth:** User required

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
      "selected": true
    }
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
  "data": {...}
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

## 1️⃣4️⃣ USER MANAGEMENT

Base: `/api/users`  
**Auth:** User required

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
  "phoneNumber": "0987654321"
}
avatarFile: [binary]

Response:
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "data": {
    "id": "user_id",
    "fullName": "Nguyễn Văn A Updated",
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
DELETE /api/users/{userId}
Authorization: Bearer {user_token}

Response:
{
  "success": true,
  "message": "Xóa tài khoản thành công",
  "data": "User deleted"
}
```

---

## 1️⃣5️⃣ ADMIN - USER MANAGEMENT

Base: `/api/admin/users`  
**Auth:** Admin role required

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
    }
  ]
}
```

### 15.2. Update User Active Status
```http
PUT /api/admin/users/active/{userId}
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
PUT /api/admin/users/{userId}/role
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

These endpoints are **DISABLED** (not available):

### ❌ Cart APIs
- `POST /api/cart/add/{userId}`
- `GET /api/cart/all`
- `GET /api/cart/{userId}`
- `DELETE /api/cart/{userId}/item/{courseId}`

### ❌ Order APIs
- `POST /api/orders/create-order`
- `PUT /api/orders/{orderId}/update-status`
- `GET /api/orders/{userId}`
- `PUT /api/orders/{orderId}/cancel`

### ❌ Admin Order APIs
- `GET /api/admin/orders/all`
- `PUT /api/admin/orders/{orderId}/status`

**Reason:** Replaced by direct payment system via `/api/payment/vnpay/create`

---

## 📊 Statistics

### Endpoints by Module:
- Authentication: 9 endpoints
- Courses (Admin): 6 endpoints
- Courses (User): 2 endpoints
- Course Categories: 4 endpoints
- Chapters (Admin): 5 endpoints
- Lessons (Admin): 6 endpoints
- Lessons (User): 8 endpoints ⬆️ (added GET progress)
- Curriculum: 4 endpoints
- Progress Tracking: 4 endpoints
- Quiz (Admin): 4 endpoints
- Quiz (User): 4 endpoints
- Payment (VNPAY): 6 endpoints
- Favorites: 7 endpoints
- User Management: 4 endpoints
- Admin User Management: 3 endpoints

**Total Active:** 76 endpoints

### Deprecated:
- Cart: 4 endpoints ❌
- Order: 4 endpoints ❌
- Admin Order: 2 endpoints ❌

**Total Deprecated:** 10 endpoints

---

## 🔐 Authentication Types

### Public (No Auth):
- Courses (User): Get all, Get by ID
- Curriculum: All endpoints
- VNPAY callbacks: Return, IPN

### User Required:
- Lessons (User): All endpoints
- Progress: All endpoints
- Quiz (User): All endpoints
- Payment: Create, Get status, Get history
- Favorites: All endpoints
- User Management: All endpoints

### Admin Required:
- Courses (Admin): All endpoints
- Categories: CUD operations
- Chapters: All endpoints
- Lessons (Admin): All endpoints
- Quiz (Admin): All endpoints
- Admin User Management: All endpoints

---

## 🆕 Latest Changes

### November 24, 2025:
1. ✅ `GET /api/admin/quizzes/all` - Get all quizzes (admin)
2. ✅ Next lesson API now includes videoUrl, videoId, videoThumbnail
3. ✅ Quiz endpoints format fixed (question, type, option IDs)

### November 23, 2025:
1. ✅ `GET /api/lessons/{id}/progress` - Get saved video progress from database
2. ✅ Video progress tracking: Auto-complete at 90%
3. ✅ Quiz integration with progress (unlock chapter requirement)
4. ✅ Access control: Check 90% video requirement for unlock
5. ✅ Video progress now uses real userId (not temp-user-id)
6. ✅ Quiz submit updates UserProgress.quizPassedAt
7. ✅ Clear messages about 90% requirement
8. ✅ Quiz passed check before unlocking next chapter
9. ❌ Removed: All Cart endpoints (replaced by direct payment)
10. ❌ Removed: All Order endpoints (replaced by direct payment)

---

## 📝 Response Format

All APIs follow this format:

### Success:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error:
```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

---

## 🔗 Resources

- **Postman Collection:** `Test_PostMan_23-11-2025.postman_collection.json`
- **Frontend Guide:** `FRONTEND_VIDEO_TRACKING_GUIDE.md`
- **Bug Fix Report:** `BUG_FIX_VIDEO_PROGRESS_USER_ID.md`
- **Base URL:** `http://localhost:8080`

---

**Last Updated:** November 24, 2025  
**Total Endpoints:** 77 Active + 10 Deprecated = 87 Total  
**Status:** ✅ Production Ready


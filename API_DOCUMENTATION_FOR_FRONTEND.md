# 📚 API DOCUMENTATION - HỆ THỐNG HỌC TẬP TRỰC TUYẾN

## 🎯 BASE URL

```
Development: http://localhost:8080
Production: https://your-domain.com
```

---

## 🔐 AUTHENTICATION

Hầu hết APIs yêu cầu JWT token trong header:

```http
Authorization: Bearer {token}
```

**Lấy token:** Gọi API đăng nhập, nhận `accessToken` từ response.

---

## 📋 TABLE OF CONTENTS

1. [Authentication](#1-authentication)
2. [Courses](#2-courses)
3. [My Courses](#3-my-courses)
4. [Chapters & Lessons](#4-chapters--lessons)
5. [Learning Progress](#5-learning-progress)
6. [Quiz](#6-quiz)
7. [Favorites](#7-favorites)
8. [Cart & Orders](#8-cart--orders)

---

## 1. AUTHENTICATION

### 1.1. Đăng ký

```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123",
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0123456789"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công!",
  "data": {
    "id": "user123",
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "USER"
  }
}
```

---

### 1.2. Đăng nhập

```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "user123",
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER"
    }
  }
}
```

**⚠️ Lưu token vào localStorage hoặc cookie để dùng cho các API sau.**

---

### 1.3. Quên mật khẩu

```http
POST /api/auth/forgot-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

---

### 1.4. Đặt lại mật khẩu

```http
POST /api/auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "token": "reset-token-from-email",
  "newPassword": "NewPassword123"
}
```

---

## 2. COURSES

### 2.1. Lấy danh sách khóa học (Public)

```http
GET /api/courses?page=0&size=10&categoryCode=PROGRAM
```

**Query Parameters:**
- `page` (optional): Số trang (default: 0)
- `size` (optional): Số items mỗi trang (default: 10)
- `categoryCode` (optional): Lọc theo danh mục

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "course123",
      "categoryCode": "PROGRAM",
      "title": "Java Spring Boot từ A-Z",
      "description": "Học Spring Boot từ cơ bản đến nâng cao",
      "price": 2000000,
      "thumbnailUrl": "http://localhost:8080/uploads/products/java.jpg",
      "duration": 50,
      "level": "Advanced",
      "instructorName": "Nguyễn Văn A",
      "rating": 4.8,
      "totalStudents": 1250,
      "discountPercent": 10,
      "discountedPrice": 1800000
    }
  ]
}
```

---

### 2.2. Chi tiết khóa học (Public)

```http
GET /api/courses/{courseId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "course123",
    "title": "Java Spring Boot từ A-Z",
    "description": "Khóa học toàn diện về Spring Boot",
    "price": 2000000,
    "discountedPrice": 1800000,
    "thumbnailUrl": "...",
    "level": "Advanced",
    "duration": 50,
    "instructorName": "Nguyễn Văn A",
    "rating": 4.8,
    "totalStudents": 1250,
    
    "overview": {
      "description": "Mô tả chi tiết khóa học...",
      "whatYouWillLearn": [
        "Spring Boot cơ bản",
        "Spring Security",
        "RESTful API",
        "Database với JPA"
      ],
      "requirements": [
        "Biết Java cơ bản",
        "Hiểu OOP"
      ],
      "targetAudience": [
        "Sinh viên IT",
        "Lập trình viên muốn học Spring Boot"
      ],
      "introVideoUrl": "https://youtube.com/...",
      "stats": {
        "totalChapters": 10,
        "totalLessons": 50,
        "totalDuration": 3000,
        "certificateProvided": true
      }
    }
  }
}
```

**💡 Sử dụng cho:** Trang chi tiết khóa học (landing page)

---

### 2.3. Xem trước Curriculum (Public)

```http
GET /api/curriculum/course/{courseId}/chapters
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "chapter1",
      "title": "Chương 1: Giới thiệu Spring Boot",
      "description": "Làm quen với Spring Boot",
      "order": 1,
      "totalLessons": 5,
      "totalDuration": 90,
      "isFree": true
    }
  ]
}
```

---

### 2.4. Xem Lessons trong Chapter (Public)

```http
GET /api/curriculum/chapters/{chapterId}/lessons
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "lesson1",
      "title": "Bài 1: Spring Boot là gì?",
      "description": "Giới thiệu về Spring Boot",
      "order": 1,
      "duration": 15,
      "isFree": true,
      "hasQuiz": false,
      "videoUrl": "https://youtube.com/watch?v=...",
      "videoType": "YOUTUBE"
    },
    {
      "id": "quiz1",
      "title": "Quiz Chương 1",
      "order": 5,
      "duration": 10,
      "isFree": false,
      "hasQuiz": true
    }
  ]
}
```

**💡 Sử dụng cho:** Preview curriculum trước khi mua

---

## 3. MY COURSES

### 3.1. Đăng ký/Mua khóa học

```http
POST /api/progress/enroll/{courseId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký khóa học thành công!",
  "data": {
    "userId": "user123",
    "courseId": "course123",
    "enrolledAt": "2025-11-21T10:00:00",
    "totalProgress": 0,
    "completedLessons": []
  }
}
```

---

### 3.2. Lấy danh sách "My Courses"

```http
GET /api/progress/my-courses
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "My courses retrieved successfully",
  "data": [
    {
      "courseId": "course123",
      "title": "Java Spring Boot từ A-Z",
      "thumbnailUrl": "http://localhost:8080/uploads/products/java.jpg",
      "instructorName": "Nguyễn Văn A",
      "level": "Advanced",
      
      "totalProgress": 45,
      "completedLessons": 9,
      "totalLessons": 20,
      "currentLessonId": "lesson10",
      "currentLessonTitle": "Bài 10: Spring Security",
      
      "enrolledAt": "2025-11-15T10:00:00",
      "lastAccessedAt": "2025-11-21T14:30:00",
      "completedAt": null,
      "isCompleted": false
    }
  ]
}
```

**💡 Sử dụng cho:** Trang "Khóa học của tôi"

**UI Suggestion:**
```jsx
<div className="my-courses">
  {myCourses.map(course => (
    <Card key={course.courseId}>
      <img src={course.thumbnailUrl} />
      <h3>{course.title}</h3>
      <ProgressBar percent={course.totalProgress} />
      <p>{course.completedLessons}/{course.totalLessons} bài</p>
      <button onClick={() => navigate(`/learn/${course.courseId}`)}>
        Tiếp tục học
      </button>
    </Card>
  ))}
</div>
```

---

### 3.3. Lấy Progress của một khóa học

```http
GET /api/progress/course/{courseId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "courseId": "course123",
    "totalProgress": 45,
    "completedLessons": ["lesson1", "lesson2", "lesson3"],
    "currentLessonId": "lesson4",
    "enrolledAt": "2025-11-15T10:00:00",
    "lastAccessedAt": "2025-11-21T14:30:00",
    "lessonsProgress": [
      {
        "lessonId": "lesson1",
        "completed": true,
        "videoProgress": 100,
        "completedAt": "2025-11-15T11:00:00"
      }
    ]
  }
}
```

---

## 4. CHAPTERS & LESSONS

### 4.1. Lấy Chapters kèm Progress

```http
GET /api/progress/course/{courseId}/chapters
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Chapters with progress retrieved",
  "data": [
    {
      "chapterId": "chapter1",
      "title": "Chương 1: Spring Boot Cơ Bản",
      "description": "Làm quen với Spring Boot",
      "order": 1,
      "totalLessons": 5,
      "totalDuration": 90,
      
      "isUnlocked": true,
      "completedLessons": 5,
      "progressPercent": 100,
      
      "finalQuizId": "quiz1",
      "hasFinalQuiz": true,
      "quizPassed": true,
      "quizScore": 85
    },
    {
      "chapterId": "chapter2",
      "title": "Chương 2: Spring Security",
      "order": 2,
      "totalLessons": 8,
      
      "isUnlocked": true,
      "completedLessons": 3,
      "progressPercent": 37,
      
      "hasFinalQuiz": true,
      "quizPassed": false,
      "quizScore": null
    },
    {
      "chapterId": "chapter3",
      "title": "Chương 3: Advanced Topics",
      "order": 3,
      
      "isUnlocked": false,
      "completedLessons": 0,
      "progressPercent": 0,
      
      "hasFinalQuiz": true,
      "quizPassed": null
    }
  ]
}
```

**💡 Sử dụng cho:** Trang học tập, hiển thị chapters với trạng thái

**UI Suggestion:**
```jsx
<div className="chapters">
  {chapters.map(chapter => (
    <ChapterCard 
      key={chapter.chapterId}
      locked={!chapter.isUnlocked}
    >
      <h3>{chapter.title}</h3>
      <ProgressBar percent={chapter.progressPercent} />
      <p>{chapter.completedLessons}/{chapter.totalLessons} bài</p>
      
      {chapter.hasFinalQuiz && (
        <QuizBadge 
          passed={chapter.quizPassed}
          score={chapter.quizScore}
        />
      )}
      
      {chapter.isUnlocked ? (
        <button onClick={() => openChapter(chapter.chapterId)}>
          Học
        </button>
      ) : (
        <div className="locked">
          🔒 Hoàn thành Chapter {chapter.order - 1} để unlock
        </div>
      )}
    </ChapterCard>
  ))}
</div>
```

---

### 4.2. Lấy chi tiết Lesson

```http
GET /api/lessons/{lessonId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "lesson1",
    "chapterId": "chapter1",
    "courseId": "course123",
    "title": "Bài 1: Spring Boot là gì?",
    "description": "Giới thiệu về Spring Boot framework",
    "order": 1,
    "duration": 15,
    "isFree": true,
    
    "videoUrl": "https://www.youtube.com/watch?v=UB1O30fR-EE",
    "videoId": "UB1O30fR-EE",
    "videoType": "YOUTUBE",
    
    "content": "# Giới thiệu Spring Boot\n\nSpring Boot là...",
    "contentType": "MARKDOWN",
    
    "hasQuiz": false
  }
}
```

**💡 Sử dụng để:** Hiển thị nội dung lesson

**UI Suggestion:**
```jsx
<div className="lesson-view">
  {/* Video Player */}
  <VideoPlayer 
    videoId={lesson.videoId}
    videoType={lesson.videoType}
    onProgress={handleProgress}
  />
  
  {/* Content */}
  <div className="lesson-content">
    <h1>{lesson.title}</h1>
    <ReactMarkdown>{lesson.content}</ReactMarkdown>
  </div>
</div>
```

---

## 5. LEARNING PROGRESS

### 5.1. Cập nhật tiến độ xem video

```http
POST /api/lessons/{lessonId}/progress?percent=50
Authorization: Bearer {token}
```

**Parameters:**
- `percent`: 0-100 (phần trăm đã xem)

**Response:**
```json
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "userId": "user123",
    "courseId": "course123",
    "currentLessonId": "lesson1",
    "totalProgress": 45,
    "lessonsProgress": [
      {
        "lessonId": "lesson1",
        "videoProgress": 50,
        "completed": false
      }
    ]
  }
}
```

**💡 Gọi API này:**
- Mỗi 10% video progress
- Hoặc mỗi 30 giây
- Khi đạt 90% → Auto-complete lesson

**Frontend Example:**
```javascript
const videoPlayer = useRef();

const handleVideoProgress = async () => {
  const percent = (currentTime / duration) * 100;
  
  if (percent >= 90 && !lessonCompleted) {
    // Auto-complete at 90%
    await updateProgress(lessonId, 90);
    setLessonCompleted(true);
    
    // Get next lesson
    const nextInfo = await getNextLesson(lessonId);
    showNextLessonModal(nextInfo);
  }
};
```

---

### 5.2. Lấy thông tin Lesson tiếp theo

```http
GET /api/lessons/{lessonId}/next
Authorization: Bearer {token}
```

**Response (Có lesson tiếp):**
```json
{
  "success": true,
  "message": "Next lesson info retrieved",
  "data": {
    "completed": true,
    "totalProgress": 50,
    "completedLessons": 10,
    "totalLessons": 20,
    
    "nextLesson": {
      "id": "lesson11",
      "title": "Bài 11: Spring Data JPA",
      "description": "Làm việc với database",
      "duration": 20,
      "chapterId": "chapter2",
      "chapterTitle": "Chương 2: Database",
      "order": 1,
      "isFree": false,
      "hasQuiz": false,
      "isUnlocked": true
    },
    
    "message": "✅ Lesson hoàn thành! Chuyển sang bài tiếp theo.",
    "courseCompleted": false,
    "suggestedAction": null
  }
}
```

**Response (Quiz chưa pass):**
```json
{
  "success": true,
  "data": {
    "completed": true,
    "totalProgress": 40,
    
    "nextLesson": null,
    
    "message": "❌ Bạn cần đạt điểm tối thiểu để unlock lesson tiếp theo. Hãy làm lại quiz!",
    "courseCompleted": false,
    "suggestedAction": "RETAKE_QUIZ",
    "requiredLessonId": "quiz1"
  }
}
```

**Response (Hoàn thành khóa học):**
```json
{
  "success": true,
  "data": {
    "completed": true,
    "totalProgress": 100,
    "nextLesson": null,
    "message": "🎉 Chúc mừng! Bạn đã hoàn thành khóa học!",
    "courseCompleted": true,
    "suggestedAction": "COURSE_DONE"
  }
}
```

**💡 Frontend Logic:**
```javascript
const handleLessonComplete = async (lessonId) => {
  const response = await fetch(`/api/lessons/${lessonId}/next`);
  const data = await response.json();
  
  if (data.data.nextLesson) {
    // Có lesson tiếp
    showNextLessonModal({
      nextLesson: data.data.nextLesson,
      onContinue: () => navigate(`/lessons/${data.data.nextLesson.id}`)
    });
  } else if (data.data.suggestedAction === 'RETAKE_QUIZ') {
    // Quiz chưa pass
    showRetakeModal({
      message: data.data.message,
      onRetry: () => navigate(`/lessons/${data.data.requiredLessonId}`)
    });
  } else if (data.data.suggestedAction === 'COURSE_DONE') {
    // Hoàn thành
    showCongratulations();
  }
};
```

---

## 6. QUIZ

### 6.1. Nộp bài Quiz

```http
POST /api/lessons/quiz/submit
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "lessonId": "quiz1",
  "answers": [
    {
      "questionId": "q1",
      "selectedOptions": ["option_a"]
    },
    {
      "questionId": "q2",
      "selectedOptions": ["option_b", "option_c"]
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Quiz submitted",
  "data": {
    "score": 85,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "passed": true,
    "results": [
      {
        "questionId": "q1",
        "correct": true,
        "userAnswers": ["option_a"],
        "correctAnswers": ["option_a"],
        "explanation": "Spring Boot giúp..."
      },
      {
        "questionId": "q2",
        "correct": false,
        "userAnswers": ["option_b"],
        "correctAnswers": ["option_a", "option_c"],
        "explanation": "Đáp án đúng là..."
      }
    ]
  }
}
```

**💡 Frontend:**
```javascript
const submitQuiz = async (answers) => {
  const response = await fetch('/api/lessons/quiz/submit', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      lessonId: quizId,
      answers: answers
    })
  });
  
  const data = await response.json();
  
  if (data.data.passed) {
    showPassModal(data.data.score);
  } else {
    showFailModal(data.data.score, data.data.results);
  }
};
```

---

## 7. FAVORITES

### 7.1. Thêm khóa học vào Favorites

```http
POST /api/favorites/add
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": "course123",
  "title": "Java Spring Boot",
  "thumbnailUrl": "http://localhost:8080/uploads/...",
  "price": 2000000,
  "discountedPrice": 1800000,
  "discountPercent": 10,
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
  "message": "Đã thêm vào yêu thích"
}
```

---

### 7.2. Xóa khỏi Favorites

```http
DELETE /api/favorites/{courseId}
Authorization: Bearer {token}
```

---

### 7.3. Lấy danh sách Favorites

```http
GET /api/favorites
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "items": [
      {
        "courseId": "course123",
        "title": "Java Spring Boot",
        "thumbnailUrl": "...",
        "price": 2000000,
        "discountedPrice": 1800000,
        "addedAt": "2025-11-20T10:00:00"
      }
    ]
  }
}
```

---

## 8. CART & ORDERS

### 8.1. Thêm vào giỏ hàng

```http
POST /api/cart/add
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": "course123",
  "title": "Java Spring Boot",
  "thumbnailUrl": "...",
  "price": 2000000,
  "discountedPrice": 1800000,
  "discountPercent": 10,
  "instructorName": "Nguyễn Văn A"
}
```

---

### 8.2. Xem giỏ hàng

```http
GET /api/cart
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "items": [
      {
        "courseId": "course123",
        "title": "Java Spring Boot",
        "price": 2000000,
        "discountedPrice": 1800000
      }
    ],
    "totalItems": 1,
    "totalPrice": 1800000
  }
}
```

---

### 8.3. Tạo đơn hàng

```http
POST /api/orders/create
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "items": [
    {
      "courseId": "course123",
      "title": "Java Spring Boot",
      "price": 1800000
    }
  ],
  "paymentMethod": "BANK_TRANSFER",
  "totalAmount": 1800000
}
```

---

## 📱 FRONTEND FLOW EXAMPLE

### Flow 1: Mua khóa học

```javascript
// 1. User vào trang chi tiết khóa học
GET /api/courses/{courseId}

// 2. Xem curriculum preview
GET /api/curriculum/course/{courseId}/chapters

// 3. Thêm vào giỏ hàng
POST /api/cart/add

// 4. Thanh toán
POST /api/orders/create

// 5. Enroll khóa học (sau khi thanh toán thành công)
POST /api/progress/enroll/{courseId}
```

---

### Flow 2: Học bài

```javascript
// 1. Vào "My Courses"
GET /api/progress/my-courses

// 2. Click "Tiếp tục học" → Xem chapters
GET /api/progress/course/{courseId}/chapters

// 3. Click chapter → Xem lessons
GET /api/curriculum/chapters/{chapterId}/lessons

// 4. Click lesson → Xem nội dung
GET /api/lessons/{lessonId}

// 5. Xem video → Update progress
POST /api/lessons/{lessonId}/progress?percent=90

// 6. Hoàn thành → Lấy lesson tiếp
GET /api/lessons/{lessonId}/next

// 7. Auto navigate hoặc show modal
```

---

## ⚠️ ERROR HANDLING

### Common Error Responses

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "message": "Bạn không có quyền truy cập lesson này. Vui lòng mua khóa học."
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "Không tìm thấy khóa học"
}
```

**400 Bad Request:**
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

---

## 📝 NOTES FOR FRONTEND

### 1. Video Player

**YouTube:**
```jsx
<iframe
  src={`https://www.youtube.com/embed/${videoId}`}
  allow="accelerometer; autoplay; encrypted-media"
  allowFullScreen
/>
```

**Track progress:**
```javascript
videoPlayer.on('timeupdate', () => {
  const percent = (currentTime / duration) * 100;
  if (percent % 10 === 0) {
    updateProgress(lessonId, percent);
  }
});
```

---

### 2. Auto Next Lesson

```javascript
const handleVideoComplete = async () => {
  // At 90% → Auto-complete
  await updateProgress(lessonId, 90);
  
  // Get next
  const nextInfo = await getNextLesson(lessonId);
  
  if (nextInfo.nextLesson) {
    showModal({
      title: '✅ Hoàn thành!',
      message: nextInfo.message,
      nextLesson: nextInfo.nextLesson,
      onContinue: () => navigate(`/lessons/${nextInfo.nextLesson.id}`)
    });
  }
};
```

---

### 3. Chapter Lock/Unlock

```jsx
{chapters.map(chapter => (
  <ChapterCard key={chapter.chapterId}>
    {chapter.isUnlocked ? (
      <button onClick={() => openChapter(chapter.chapterId)}>
        Học
      </button>
    ) : (
      <div className="locked-overlay">
        <LockIcon />
        <p>Hoàn thành Quiz Chương {chapter.order - 1}</p>
      </div>
    )}
  </ChapterCard>
))}
```

---

## 🎯 QUICK START

```javascript
// 1. Login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { accessToken } = await loginResponse.json();
localStorage.setItem('token', accessToken);

// 2. Get My Courses
const coursesResponse = await fetch('/api/progress/my-courses', {
  headers: { 'Authorization': `Bearer ${accessToken}` }
});
const { data: myCourses } = await coursesResponse.json();

// 3. Learn
navigate(`/learn/${myCourses[0].courseId}`);
```

---

**🎉 Done! Bạn có thể bắt đầu implement Frontend với các APIs này!** 🚀


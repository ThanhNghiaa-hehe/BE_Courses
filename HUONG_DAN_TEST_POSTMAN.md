# 🧪 HƯỚNG DẪN TEST API - LESSON SYSTEM

## 📦 FILE POSTMAN COLLECTION

**File:** `Complete_Lesson_API_Collection.postman_collection.json`

**Tổng số APIs:** 28 endpoints

---

## 🚀 BƯỚC 1: IMPORT VÀO POSTMAN

1. Mở Postman
2. Click **Import** (góc trên bên trái)
3. Chọn file `Complete_Lesson_API_Collection.postman_collection.json`
4. Click **Import**

✅ Collection sẽ xuất hiện ở sidebar bên trái

---

## ⚙️ BƯỚC 2: CẤU HÌNH VARIABLES

### Mở Collection Variables:
1. Right-click vào collection **"Lesson System - Complete API Test Collection"**
2. Chọn **Edit**
3. Tab **Variables**

### Cập nhật giá trị:

| Variable | Current Value | Mô tả |
|----------|---------------|-------|
| `baseUrl` | `http://localhost:8080` | Không cần đổi |
| `adminToken` | **PASTE TOKEN** | Token sau khi login admin |
| `userToken` | **PASTE TOKEN** | Token sau khi login user |
| `courseId` | **PASTE COURSE ID** | ID khóa học để test |
| `chapterId` | Tự động | Tự động lưu sau khi tạo chapter |
| `lessonId` | Tự động | Tự động lưu sau khi tạo lesson |
| `quizLessonId` | Tự động | Tự động lưu sau khi tạo quiz |

---

## 🔥 BƯỚC 3: TEST THEO THỨ TỰ

### **FOLDER 0: Authentication** (Bắt buộc chạy đầu tiên)

#### 0.1 Login Admin
```http
POST /api/auth/login
Body: {
  "email": "admin@example.com",
  "password": "admin123"
}
```
✅ **Auto-save token** vào `adminToken` variable

#### 0.2 Login User
```http
POST /api/auth/login
Body: {
  "email": "user@example.com",
  "password": "user123"
}
```
✅ **Auto-save token** vào `userToken` variable

---

### **FOLDER 1: ADMIN - Chapter Management** (6 APIs)

#### 1.1 Create Chapter ⭐
```http
POST /api/admin/chapters/create

Body:
{
  "courseId": "{{courseId}}",
  "title": "Chương 1: Làm quen HTML",
  "description": "Giới thiệu HTML cơ bản...",
  "order": 1,
  "isFree": true
}
```
✅ **Auto-save** `chapterId` để dùng cho các request sau

**Expected Response:**
```json
{
  "success": true,
  "message": "Chapter created successfully",
  "data": {
    "id": "abc123",
    "courseId": "...",
    "title": "Chương 1: Làm quen HTML",
    "order": 1,
    "totalLessons": 0,
    "totalDuration": 0,
    "isFree": true
  }
}
```

#### 1.2 Create Chapter 2
Tạo thêm chapter thứ 2 (không bắt buộc)

#### 1.3 Get All Chapters by Course ⭐
```http
GET /api/admin/chapters/course/{{courseId}}
```
Xem tất cả chapters đã tạo

#### 1.4 Get Chapter by ID
```http
GET /api/admin/chapters/{{chapterId}}
```

#### 1.5 Update Chapter
```http
PUT /api/admin/chapters/{{chapterId}}
Body: {"title": "Chương 1 Updated"}
```

#### 1.6 Delete Chapter
```http
DELETE /api/admin/chapters/{{chapterId}}
```
⚠️ **Cẩn thận:** Xóa cả lessons bên trong!

---

### **FOLDER 2: ADMIN - Lesson Management** (9 APIs)

#### 2.1 Create Lesson (Basic - Free) ⭐⭐⭐
```http
POST /api/admin/lessons/create

Body:
{
  "chapterId": "{{chapterId}}",
  "courseId": "{{courseId}}",
  "title": "Bài 1: Giới thiệu HTML",
  "order": 1,
  "duration": 15,
  "isFree": true,
  "videoUrl": "https://www.youtube.com/watch?v=UB1O30fR-EE",
  "videoType": "YOUTUBE",
  "contentType": "MARKDOWN",
  "content": "# Giới thiệu HTML\n\n..."
}
```
✅ **Auto-save** `lessonId`

**Features:**
- ✅ YouTube video auto-extract ID
- ✅ Markdown content
- ✅ Free lesson

#### 2.2 Create Lesson with Code Snippets ⭐⭐
```http
Lesson với requiredPreviousLesson và code snippets
```

**Highlights:**
- ✅ Unlock tuần tự (cần hoàn thành lesson trước)
- ✅ Code snippets với syntax highlighting

#### 2.3 Create Lesson with Attachments
```http
Lesson với file đính kèm (PDF, ZIP)
```

#### 2.4 Create Lesson with Quiz ⭐⭐⭐
```http
POST /api/admin/lessons/create

Quiz có 5 câu hỏi:
- 3 SINGLE_CHOICE
- 1 MULTIPLE_CHOICE
- 1 TRUE_FALSE
```
✅ **Auto-save** `quizLessonId`

**Quiz Features:**
- ✅ Passing score: 70%
- ✅ Time limit: 15 phút
- ✅ Unlimited attempts
- ✅ Giải thích đáp án

#### 2.5 Get Lessons by Chapter
```http
GET /api/admin/lessons/chapter/{{chapterId}}
```

#### 2.6 Get All Lessons by Course ⭐
```http
GET /api/admin/lessons/course/{{courseId}}
```
Xem toàn bộ curriculum

#### 2.7 Get Lesson by ID
```http
GET /api/admin/lessons/{{lessonId}}
```
✅ Tự động tăng view count

#### 2.8 Update Lesson
```http
PUT /api/admin/lessons/{{lessonId}}
```

#### 2.9 Delete Lesson
```http
DELETE /api/admin/lessons/{{lessonId}}
```

---

### **FOLDER 3: USER - Progress Management** (2 APIs)

#### 3.1 Enroll Course ⭐⭐⭐
```http
POST /api/progress/enroll/{{courseId}}
```
Bắt buộc chạy trước khi học!

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "courseId": "course456",
    "completedLessons": [],
    "totalProgress": 0,
    "enrolledAt": "2025-11-21T..."
  }
}
```

#### 3.2 Get My Progress ⭐
```http
GET /api/progress/course/{{courseId}}
```
Xem tiến độ học tập

---

### **FOLDER 4: USER - Lesson Learning** (7 APIs)

#### 4.1 Get Lesson Details ⭐
```http
GET /api/lessons/{{lessonId}}
```
Xem nội dung bài học

#### 4.2 Check Access Permission
```http
GET /api/lessons/{{lessonId}}/access
```
Kiểm tra có quyền xem không

**Response:**
```json
{
  "success": true,
  "message": "Access granted",
  "data": true
}
```

#### 4.3-4.5 Update Video Progress ⭐⭐
```http
POST /api/lessons/{{lessonId}}/progress?percent=25
POST /api/lessons/{{lessonId}}/progress?percent=50
POST /api/lessons/{{lessonId}}/progress?percent=90  ← Tự động mark complete
```

**Logic:**
- 0-89%: Chỉ update progress
- ≥90%: **Tự động đánh dấu hoàn thành**

#### 4.6 Mark Lesson Complete (Manual)
```http
POST /api/lessons/{{lessonId}}/complete
```
Đánh dấu hoàn thành thủ công

#### 4.7 Like Lesson
```http
POST /api/lessons/{{lessonId}}/like
```

---

### **FOLDER 5: USER - Quiz Submission** (3 APIs)

#### 5.1 Submit Quiz (Pass - 100%) ⭐⭐⭐
```http
POST /api/lessons/quiz/submit

Body:
{
  "lessonId": "{{quizLessonId}}",
  "answers": [
    {"questionId": "q1", "selectedOptions": ["a"]},
    {"questionId": "q2", "selectedOptions": ["a"]},
    {"questionId": "q3", "selectedOptions": ["a", "b", "d"]},
    {"questionId": "q4", "selectedOptions": ["a"]},
    {"questionId": "q5", "selectedOptions": ["b"]}
  ]
}
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "score": 100,
    "totalQuestions": 5,
    "correctAnswers": 5,
    "passed": true,
    "results": [
      {
        "questionId": "q1",
        "correct": true,
        "userAnswers": ["a"],
        "correctAnswers": ["a"],
        "explanation": "HTML viết tắt của..."
      }
    ]
  }
}
```

#### 5.2 Submit Quiz (Fail - 40%)
Test trường hợp KHÔNG pass (< 70%)

#### 5.3 Submit Quiz (Pass - 80%)
Test trường hợp pass vừa đủ

---

## 📊 KỊCH BẢN TEST ĐẦY ĐỦ

### 🎯 Kịch bản 1: Admin tạo khóa học

```
1. Login Admin (0.1)
2. Create Chapter 1 (1.1) ✅ Save chapterId
3. Create Lesson 1 - Free (2.1) ✅ Save lessonId
4. Create Lesson 2 - Paid, unlock tuần tự (2.2)
5. Create Lesson 3 - With attachments (2.3)
6. Create Quiz Lesson (2.4) ✅ Save quizLessonId
7. Get All Lessons by Course (2.6) → Xem curriculum
```

### 🎓 Kịch bản 2: User học khóa học

```
1. Login User (0.2)
2. Enroll Course (3.1) ✅ Khởi tạo progress
3. Get Lesson 1 (4.1) → Xem nội dung
4. Update Video Progress 25% (4.3)
5. Update Video Progress 50% (4.4)
6. Update Video Progress 90% (4.5) ✅ Auto complete
7. Get My Progress (3.2) → Xem totalProgress = 25%
8. Submit Quiz (5.1) ✅ Pass → Unlock chapter tiếp
9. Get My Progress (3.2) → totalProgress = 50%
```

---

## ✅ CHECKLIST TEST

### Phase 1: Setup
- [ ] Import collection vào Postman
- [ ] Login Admin → Copy token
- [ ] Login User → Copy token
- [ ] Paste courseId vào variables

### Phase 2: Admin tạo nội dung
- [ ] Tạo Chapter 1
- [ ] Tạo Lesson 1 (free)
- [ ] Tạo Lesson 2 (unlock tuần tự)
- [ ] Tạo Quiz Lesson
- [ ] Xem all lessons → Verify

### Phase 3: User học
- [ ] Enroll course
- [ ] Xem lesson
- [ ] Update video progress (25%, 50%, 90%)
- [ ] Verify auto-complete tại 90%
- [ ] Submit quiz (fail)
- [ ] Submit quiz (pass)
- [ ] Xem progress → Verify %

### Phase 4: Edge cases
- [ ] Access lesson chưa unlock → Expect 403
- [ ] Submit quiz sai format → Expect error
- [ ] Delete chapter → Verify cascade delete

---

## 🔧 XỬ LÝ LỖI THƯỜNG GẶP

### ❌ Lỗi 401 Unauthorized
**Nguyên nhân:** Token hết hạn hoặc chưa paste
**Giải pháp:** Login lại và paste token mới

### ❌ Lỗi 403 Forbidden
**Nguyên nhân:** 
- User chưa enroll course
- Lesson trước chưa complete
**Giải pháp:** Enroll trước, hoàn thành lesson theo thứ tự

### ❌ Lỗi 404 Not Found
**Nguyên nhân:** 
- chapterId/lessonId sai
- Lesson đã bị xóa
**Giải pháp:** Check lại variables, tạo lại nếu cần

### ❌ Response trống
**Nguyên nhân:** courseId sai
**Giải pháp:** Get courseId từ `GET /api/admin/courses/getAll`

---

## 📈 MẸO TEST HIỆU QUẢ

### 1. Dùng Tests tab để auto-save variables
```javascript
// Đã có sẵn trong collection
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.collectionVariables.set('chapterId', jsonData.data.id);
}
```

### 2. Chạy toàn bộ folder
- Right-click folder → **Run folder**
- Tự động chạy tất cả requests trong folder

### 3. Export kết quả
- Collection Runner → Run → Export results
- Lưu lại để báo cáo

### 4. Environment cho nhiều môi trường
```
Development: localhost:8080
Staging: staging.example.com
Production: api.example.com
```

---

## 📚 TÀI LIỆU THAM KHẢO

- **Hướng dẫn hệ thống:** `HUONG_DAN_HE_THONG_LESSON.md`
- **Tóm tắt:** `TOM_TAT_HE_THONG_LESSON.md`
- **Fix lỗi build:** `FIX_BUILD_ERRORS.md`

---

## 🎉 HOÀN THÀNH

✅ **28 API endpoints** sẵn sàng test
✅ **Auto-save variables** để test nhanh
✅ **Full scenarios** từ admin đến user

**Happy Testing! 🚀**


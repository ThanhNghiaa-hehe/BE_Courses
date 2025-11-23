# 🔥 CRITICAL BUG FIX - Video Progress & User ID

## Vấn đề phát hiện
Video progress và quiz tracking đang sử dụng **hardcoded userId = "temp-user-id"** thay vì userId thật từ JWT token.

## Hậu quả
- ❌ Tất cả users dùng chung progress
- ❌ Video progress không được lưu đúng user
- ❌ Quiz results không liên kết đúng user
- ❌ Access control bị vô hiệu hóa
- ❌ Multi-user system hoàn toàn hỏng

## Đã sửa

### 1. LessonUserController
**File:** `src/main/java/com/example/cake/lesson/controller/LessonUserController.java`

**Thay đổi:**
```java
// ❌ BEFORE
String userId = "temp-user-id";

// ✅ AFTER
private String getUserId(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
}

String userId = getUserId(authentication);
```

**Endpoints đã sửa:**
- ✅ `POST /{id}/progress` - Update video progress **[CRITICAL]**
- ✅ `POST /{id}/complete` - Mark lesson complete
- ✅ `GET /{id}/access` - Check access
- ✅ `GET /{id}/next` - Get next lesson
- ✅ `GET /{id}` - Get lesson (enabled access check)

### 2. QuizUserController
**File:** `src/main/java/com/example/cake/quiz/controller/QuizUserController.java`

**Endpoints đã sửa:**
- ✅ `POST /submit` - Submit quiz
- ✅ `GET /{quizId}/attempts` - Get attempts
- ✅ `GET /{quizId}/passed` - Check passed

## Kiểm tra
```bash
# Compile thành công
mvn clean compile
# Result: BUILD SUCCESS ✅

# Không còn hardcoded ID
grep -r "temp-user-id" src/
# Result: No matches ✅
```

## Cần làm gì tiếp theo

### 1. Test ngay
```bash
# Test update video progress
curl -X POST http://localhost:8080/api/lessons/{lessonId}/progress?percent=50 \
  -H "Authorization: Bearer {user_token}"

# Test với 2 users khác nhau - mỗi user phải có progress riêng
```

### 2. Clean bad data (nếu đã có data test)
```javascript
// MongoDB
db.userProgress.deleteMany({ userId: "temp-user-id" })
db.quizAttempts.deleteMany({ userId: "temp-user-id" })
```

### 3. Update Postman collection
Test tất cả endpoints:
- Video progress endpoints
- Lesson completion
- Quiz submission
- Access control

## Tài liệu chi tiết
Xem file: `BUG_FIX_VIDEO_PROGRESS_USER_ID.md`

---

**Priority:** 🔴 CRITICAL  
**Status:** ✅ FIXED & COMPILED  
**Next:** Testing & Deployment


# ✅ TỰ ĐỘNG CHUYỂN LESSON - ĐÃ HOÀN THÀNH

## 🎯 TỔNG QUAN

**Câu hỏi:** Sau khi xem complete video thì có tự động chuyển sang lesson tiếp theo không?

**Trả lời:** 

### Trước khi update:
❌ **KHÔNG** - Backend chỉ đánh dấu complete, không trả về thông tin lesson tiếp theo

### Sau khi update:
✅ **CÓ** - Backend tự động tìm và trả về thông tin lesson tiếp theo kèm với:
- ID lesson tiếp theo
- Tiêu đề, mô tả
- Thời lượng
- Chapter title
- Trạng thái unlock
- Message hướng dẫn

---

## 🆕 ĐÃ BỔ SUNG

### **1. Repository Methods** (Tìm lesson/chapter tiếp theo)

#### LessonRepository.java
```java
// Tìm lesson tiếp theo trong cùng chapter
Lesson findFirstByChapterIdAndOrderGreaterThanOrderByOrderAsc(
    String chapterId, 
    Integer currentOrder
);

// Tìm lesson đầu tiên của chapter
Lesson findFirstByChapterIdOrderByOrderAsc(String chapterId);
```

#### ChapterRepository.java
```java
// Tìm chapter tiếp theo trong course
Chapter findFirstByCourseIdAndOrderGreaterThanOrderByOrderAsc(
    String courseId, 
    Integer currentOrder
);
```

---

### **2. DTO: LessonCompleteResponse**

File mới: `LessonCompleteResponse.java`

```java
{
  "completed": true,
  "totalProgress": 40,           // % tổng khóa học
  "completedLessons": 2,         // Đã hoàn thành 2 lessons
  "totalLessons": 5,             // Tổng 5 lessons
  
  "nextLesson": {                // ← THÔNG TIN LESSON TIẾP THEO
    "id": "lesson3",
    "title": "Bài 3: Thuộc tính HTML",
    "description": "Tìm hiểu về attributes...",
    "duration": 18,
    "chapterId": "chapter1",
    "chapterTitle": "Chương 1: HTML Cơ Bản",
    "order": 3,
    "isFree": false,
    "hasQuiz": false,
    "isUnlocked": true           // ← Đã unlock (lesson 2 complete)
  },
  
  "message": "Lesson hoàn thành! Chuyển sang bài tiếp theo.",
  "courseCompleted": false
}
```

---

### **3. Service Methods**

#### ProgressService.java

**a) findNextLesson()**
```java
private Lesson findNextLesson(Lesson currentLesson) {
    // 1. Tìm lesson tiếp theo trong cùng chapter
    Lesson nextInChapter = lessonRepository
        .findFirstByChapterIdAndOrderGreaterThanOrderByOrderAsc(
            currentLesson.getChapterId(), 
            currentLesson.getOrder()
        );
    
    if (nextInChapter != null) {
        return nextInChapter;  // Còn lesson trong chapter
    }
    
    // 2. Hết lessons trong chapter → Tìm chapter tiếp theo
    Chapter nextChapter = chapterRepository
        .findFirstByCourseIdAndOrderGreaterThanOrderByOrderAsc(...);
    
    if (nextChapter == null) {
        return null;  // Hết khóa học
    }
    
    // 3. Lấy lesson đầu tiên của chapter tiếp theo
    return lessonRepository
        .findFirstByChapterIdOrderByOrderAsc(nextChapter.getId());
}
```

**b) createCompleteResponse()**
```java
public LessonCompleteResponse createCompleteResponse(
    UserProgress progress, 
    String courseId
) {
    // Tìm lesson tiếp theo
    Lesson nextLesson = findNextLesson(currentLesson);
    
    // Check unlock status
    boolean isUnlocked = 
        nextLesson.isFree || 
        nextLesson.requiredPreviousLesson == null ||
        progress.isLessonCompleted(previousLesson);
    
    // Build response với next lesson info
    return LessonCompleteResponse.builder()
        .nextLesson(...)
        .message("Lesson hoàn thành! Chuyển sang bài tiếp theo.")
        .build();
}
```

**c) getNextLessonInfo()** ← API endpoint
```java
public ResponseMessage<LessonCompleteResponse> getNextLessonInfo(
    String userId, 
    String currentLessonId
) {
    // Lấy progress
    // Tạo complete response với next lesson
    return new ResponseMessage<>(true, "Next lesson info", response);
}
```

---

### **4. Controller Endpoint**

#### LessonUserController.java

```java
/**
 * Lấy thông tin lesson tiếp theo sau khi complete
 */
@GetMapping("/{id}/next")
public ResponseEntity<ResponseMessage<LessonCompleteResponse>> getNextLessonInfo(
    @PathVariable String id,
    Authentication authentication
) {
    String userId = getUserId(authentication);
    return ResponseEntity.ok(progressService.getNextLessonInfo(userId, id));
}
```

---

## 🔄 LUỒNG HOẠT ĐỘNG

### **Kịch bản: User xem video đến 90%**

```
Bước 1: Frontend detect video progress = 90%
        ↓
Bước 2: POST /api/lessons/{lessonId}/progress?percent=90
        Headers: Authorization: Bearer {token}
        ↓
Bước 3: Backend auto-complete lesson
        - Mark lesson as completed ✅
        - Update totalProgress
        - Save to database
        ↓
Bước 4: Frontend nhận response (UserProgress)
        {
          "currentLessonId": "lesson2",
          "completedLessons": ["lesson1", "lesson2"],
          "totalProgress": 40
        }
        ↓
Bước 5: Frontend GỌI THÊM API để lấy next lesson
        GET /api/lessons/{lessonId}/next
        ↓
Bước 6: Backend trả về LessonCompleteResponse
        {
          "completed": true,
          "totalProgress": 40,
          "nextLesson": {
            "id": "lesson3",
            "title": "Bài 3: Thuộc tính HTML",
            "isUnlocked": true
          },
          "message": "Lesson hoàn thành! Chuyển sang bài tiếp theo."
        }
        ↓
Bước 7: Frontend hiển thị popup/modal
        ┌────────────────────────────────┐
        │  ✅ Hoàn thành bài học!        │
        │                                │
        │  Tiến độ: 40% (2/5 lessons)    │
        │                                │
        │  Bài tiếp theo:                │
        │  📚 Bài 3: Thuộc tính HTML     │
        │  ⏱️ Thời lượng: 18 phút        │
        │                                │
        │  [Tiếp tục học] [Về trang chủ] │
        └────────────────────────────────┘
        ↓
Bước 8: User click "Tiếp tục học"
        → Frontend navigate to: /lessons/lesson3
        → Tự động play video lesson tiếp theo
```

---

## 📊 CÁC TÌNH HUỐNG

### **Tình huống 1: Còn lessons trong chapter**

```javascript
// Lesson 2 complete → Lesson 3 (cùng chapter)

Current: Lesson 2 (Chapter 1, order: 2)
  ↓
Next: Lesson 3 (Chapter 1, order: 3)

Response:
{
  "nextLesson": {
    "id": "lesson3",
    "title": "Bài 3: Thuộc tính HTML",
    "chapterTitle": "Chương 1: HTML Cơ Bản",
    "order": 3,
    "isUnlocked": true
  }
}
```

---

### **Tình huống 2: Hết lessons → Chuyển chapter**

```javascript
// Lesson 5 (cuối Chapter 1) complete → Lesson 6 (đầu Chapter 2)

Current: Lesson 5 (Chapter 1, order: 5) ← Lesson cuối chapter
  ↓
Find: Chapter 2 (order > 1)


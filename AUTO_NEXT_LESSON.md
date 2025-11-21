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
  ↓
Next: Lesson 6 (Chapter 2, order: 1) ← Lesson đầu chapter mới

Response:
{
  "nextLesson": {
    "id": "lesson6",
    "title": "Bài 1: Giới thiệu CSS",
    "chapterTitle": "Chương 2: CSS Cơ Bản",  ← Chapter mới
    "order": 1,
    "isUnlocked": false  ← Có thể bị lock nếu chưa pass quiz
  },
  "message": "Lesson hoàn thành! Chuyển sang chương tiếp theo."
}
```

---

### **Tình huống 3: Hoàn thành khóa học**

```javascript
// Lesson cuối cùng complete → Không có next lesson

Current: Lesson 50 (Chapter 10, order: 5) ← Lesson cuối course
  ↓
Find: Không có chapter tiếp theo
  ↓
Next: null

Response:
{
  "completed": true,
  "totalProgress": 100,
  "completedLessons": 50,
  "totalLessons": 50,
  "nextLesson": null,  ← Không có lesson tiếp
  "message": "Chúc mừng! Bạn đã hoàn thành khóa học!",
  "courseCompleted": true  ← Hoàn thành
}
```

---

### **Tình huống 4: Next lesson bị lock**

```javascript
// Lesson tiếp theo cần complete lesson trước

Current: Lesson 2 complete
  ↓
Next: Lesson 3 (requiredPreviousLesson: "lesson2")
  ↓
Check: lesson2 đã complete? ✅ Yes
  ↓
isUnlocked: true

Response:
{
  "nextLesson": {
    "id": "lesson3",
    "isUnlocked": true  ← Đã unlock
  }
}

---

// Nếu skip lesson 2
Current: Lesson 1 complete
  ↓
Next: Lesson 3 (requiredPreviousLesson: "lesson2")
  ↓
Check: lesson2 đã complete? ❌ No
  ↓
isUnlocked: false

Response:
{
  "nextLesson": {
    "id": "lesson3",
    "isUnlocked": false  ← Vẫn locked
  },
  "message": "Vui lòng hoàn thành Bài 2 trước."
}
```

---

## 🎨 FRONTEND INTEGRATION

### **React Example - Auto Navigate**

```jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function VideoPlayer({ lessonId }) {
  const navigate = useNavigate();
  const [showNextModal, setShowNextModal] = useState(false);
  const [nextLesson, setNextLesson] = useState(null);

  // Listen video progress
  const handleVideoProgress = async (percent) => {
    if (percent >= 90) {
      // 1. Update progress (auto-complete)
      await fetch(`/api/lessons/${lessonId}/progress?percent=${percent}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      // 2. Get next lesson info
      const response = await fetch(`/api/lessons/${lessonId}/next`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await response.json();

      if (data.data.nextLesson && data.data.nextLesson.isUnlocked) {
        setNextLesson(data.data.nextLesson);
        setShowNextModal(true);  // Show modal
      } else if (data.data.courseCompleted) {
        // Show congratulations
        showCongrats();
      }
    }
  };

  const handleContinue = () => {
    // Auto navigate to next lesson
    navigate(`/lessons/${nextLesson.id}`);
  };

  return (
    <div>
      <VideoPlayer 
        onProgress={handleVideoProgress}
      />

      {/* Modal Next Lesson */}
      {showNextModal && (
        <Modal>
          <h2>✅ Hoàn thành bài học!</h2>
          <p>Tiến độ: {totalProgress}%</p>
          
          <div className="next-lesson">
            <h3>Bài tiếp theo:</h3>
            <p>{nextLesson.title}</p>
            <p>⏱️ {nextLesson.duration} phút</p>
            <p>📚 {nextLesson.chapterTitle}</p>
          </div>

          <button onClick={handleContinue}>
            Tiếp tục học →
          </button>
          <button onClick={() => setShowNextModal(false)}>
            Học lại
          </button>
        </Modal>
      )}
    </div>
  );
}
```

---

### **Vue Example - Auto Countdown**

```vue
<template>
  <div>
    <video-player @progress="handleProgress" />
    
    <!-- Auto redirect countdown -->
    <div v-if="showAutoNext" class="auto-next">
      <p>✅ Hoàn thành! Chuyển sang bài tiếp trong {{ countdown }}s...</p>
      <p>{{ nextLesson.title }}</p>
      <button @click="cancelAutoNext">Hủy</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      showAutoNext: false,
      countdown: 5,
      nextLesson: null
    }
  },
  methods: {
    async handleProgress(percent) {
      if (percent >= 90) {
        // Get next lesson
        const response = await this.$http.get(`/api/lessons/${this.lessonId}/next`);
        this.nextLesson = response.data.data.nextLesson;
        
        if (this.nextLesson?.isUnlocked) {
          this.showAutoNext = true;
          this.startCountdown();
        }
      }
    },
    
    startCountdown() {
      const timer = setInterval(() => {
        this.countdown--;
        if (this.countdown === 0) {
          clearInterval(timer);
          // Auto navigate
          this.$router.push(`/lessons/${this.nextLesson.id}`);
        }
      }, 1000);
    },
    
    cancelAutoNext() {
      this.showAutoNext = false;
      this.countdown = 5;
    }
  }
}
</script>
```

---

## 📝 API ENDPOINTS

### **Endpoint mới:**

```http
GET /api/lessons/{lessonId}/next
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Next lesson info retrieved",
  "data": {
    "completed": true,
    "totalProgress": 40,
    "completedLessons": 2,
    "totalLessons": 5,
    "nextLesson": {
      "id": "lesson3",
      "title": "Bài 3: Thuộc tính HTML",
      "description": "...",
      "duration": 18,
      "chapterId": "chapter1",
      "chapterTitle": "Chương 1: HTML Cơ Bản",
      "order": 3,
      "isFree": false,
      "hasQuiz": false,
      "isUnlocked": true
    },
    "message": "Lesson hoàn thành! Chuyển sang bài tiếp theo.",
    "courseCompleted": false
  }
}
```

---

## ✅ CHECKLIST

### Backend đã implement:
- [x] LessonRepository.findNextLesson()
- [x] ChapterRepository.findNextChapter()
- [x] ProgressService.findNextLesson()
- [x] ProgressService.createCompleteResponse()
- [x] ProgressService.getNextLessonInfo()
- [x] LessonUserController.getNextLessonInfo()
- [x] LessonCompleteResponse DTO

### Frontend cần làm:
- [ ] Listen video progress event
- [ ] Gọi API /next khi complete
- [ ] Hiển thị modal next lesson
- [ ] Button "Tiếp tục học"
- [ ] Auto navigate (optional)
- [ ] Countdown timer (optional)

---

## 🎯 TÓM TẮT

### ❌ Trước khi update:
```
Video 90% → Complete ✅
             ↓
          [Dừng lại]
          
User phải tự:
- Quay về danh sách lessons
- Tìm lesson tiếp theo
- Click vào lesson đó
```

### ✅ Sau khi update:
```
Video 90% → Complete ✅
             ↓
    GET /api/lessons/{id}/next
             ↓
    Response: {nextLesson info}
             ↓
    Frontend show modal:
    ┌─────────────────────┐
    │ ✅ Hoàn thành!      │
    │ Bài tiếp: Bài 3     │
    │ [Tiếp tục học] →    │
    └─────────────────────┘
             ↓
    Click → Auto navigate
             ↓
    Lesson 3 auto play ▶️
```

---

## 🚀 KẾT LUẬN

✅ **Backend ĐÃ BỔ SUNG đầy đủ:**
- Tìm lesson tiếp theo (cùng chapter hoặc chapter mới)
- Check unlock status
- Trả về thông tin đầy đủ cho Frontend
- Handle case hết khóa học

✅ **Frontend CHỈ CẦN:**
- Gọi API `/api/lessons/{id}/next` sau khi complete
- Hiển thị modal với info
- Navigate đến lesson tiếp theo

✅ **Trải nghiệm học tập:**
- Seamless transition giữa các lessons
- Không cần tìm kiếm thủ công
- Rõ ràng về progress và next step
- Có thể auto-play hoặc manual

**🎉 Hoàn thành! User giờ có trải nghiệm học tập liền mạch!**


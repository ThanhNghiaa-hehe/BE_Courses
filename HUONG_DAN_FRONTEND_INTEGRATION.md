# 📚 HƯỚNG DẪN SỬ DỤNG API CHO FRONTEND

## 📦 FILES ĐÃ TẠO

### 1. **API_DOCUMENTATION_FOR_FRONTEND.md**
- Tài liệu API đầy đủ
- Request/Response examples
- Frontend code examples
- Flow suggestions
- Error handling

### 2. **Complete_API_For_Frontend.postman_collection.json**
- Postman collection hoàn chỉnh
- Tất cả APIs đã test được
- Auto-save variables (token, IDs)
- Test scripts tự động

---

## 🚀 QUICK START

### Bước 1: Import Postman Collection

1. Mở Postman
2. Click **Import**
3. Chọn file `Complete_API_For_Frontend.postman_collection.json`
4. Collection sẽ tự động import với tất cả APIs

### Bước 2: Test APIs

**Thứ tự test:**

1. **Login:**
   ```
   POST /api/auth/login
   → Nhận token tự động
   ```

2. **Get Courses:**
   ```
   GET /api/courses
   → Lấy courseId tự động
   ```

3. **Enroll Course:**
   ```
   POST /api/progress/enroll/{courseId}
   → Đăng ký khóa học
   ```

4. **My Courses:**
   ```
   GET /api/progress/my-courses
   → Xem khóa học đã mua
   ```

5. **Learn:**
   ```
   GET /api/progress/course/{courseId}/chapters
   → Xem chapters với progress
   ```

---

## 💻 FRONTEND INTEGRATION

### Setup

```javascript
// config/api.js
const API_BASE_URL = 'http://localhost:8080';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  };
};

export const api = {
  get: (url) => fetch(`${API_BASE_URL}${url}`, {
    headers: getAuthHeaders()
  }),
  
  post: (url, data) => fetch(`${API_BASE_URL}${url}`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data)
  })
};
```

---

### Example: Login

```javascript
// pages/Login.jsx
const handleLogin = async (email, password) => {
  try {
    const response = await api.post('/api/auth/login', {
      email,
      password
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Save token
      localStorage.setItem('token', data.data.accessToken);
      
      // Save user info
      localStorage.setItem('user', JSON.stringify(data.data.user));
      
      // Redirect
      navigate('/my-courses');
    } else {
      showError(data.message);
    }
  } catch (error) {
    showError('Đăng nhập thất bại');
  }
};
```

---

### Example: My Courses

```javascript
// pages/MyCourses.jsx
const [courses, setCourses] = useState([]);

useEffect(() => {
  const fetchMyCourses = async () => {
    const response = await api.get('/api/progress/my-courses');
    const data = await response.json();
    
    if (data.success) {
      setCourses(data.data);
    }
  };
  
  fetchMyCourses();
}, []);

return (
  <div className="my-courses">
    <h1>Khóa học của tôi</h1>
    {courses.map(course => (
      <CourseCard key={course.courseId}>
        <img src={course.thumbnailUrl} />
        <h3>{course.title}</h3>
        <ProgressBar percent={course.totalProgress} />
        <p>{course.completedLessons}/{course.totalLessons} bài</p>
        <button onClick={() => navigate(`/learn/${course.courseId}`)}>
          Tiếp tục học
        </button>
      </CourseCard>
    ))}
  </div>
);
```

---

### Example: Video Player với Auto-Complete

```javascript
// components/VideoPlayer.jsx
const VideoPlayer = ({ lesson, onComplete }) => {
  const [progress, setProgress] = useState(0);
  const [completed, setCompleted] = useState(false);
  
  const handleProgress = async (currentTime, duration) => {
    const percent = Math.floor((currentTime / duration) * 100);
    setProgress(percent);
    
    // Update progress mỗi 10%
    if (percent % 10 === 0) {
      await api.post(`/api/lessons/${lesson.id}/progress?percent=${percent}`);
    }
    
    // Auto-complete tại 90%
    if (percent >= 90 && !completed) {
      setCompleted(true);
      
      // Get next lesson
      const response = await api.get(`/api/lessons/${lesson.id}/next`);
      const data = await response.json();
      
      if (data.data.nextLesson) {
        showNextLessonModal(data.data);
      }
    }
  };
  
  return (
    <div className="video-player">
      {lesson.videoType === 'YOUTUBE' && (
        <YouTubePlayer
          videoId={lesson.videoId}
          onProgress={handleProgress}
        />
      )}
      <ProgressBar percent={progress} />
    </div>
  );
};
```

---

## 📋 MAIN FLOWS

### Flow 1: Đăng ký & Mua khóa học

```
1. POST /api/auth/register
   → Đăng ký tài khoản

2. POST /api/auth/login
   → Đăng nhập, nhận token

3. GET /api/courses/{id}
   → Xem chi tiết khóa học + preview

4. POST /api/cart/add
   → Thêm vào giỏ hàng

5. POST /api/orders/create
   → Tạo đơn hàng

6. POST /api/progress/enroll/{courseId}
   → Enroll khóa học (sau thanh toán)
```

---

### Flow 2: Học bài

```
1. GET /api/progress/my-courses
   → Lấy danh sách khóa học đã mua

2. GET /api/progress/course/{courseId}/chapters
   → Xem chapters với progress

3. GET /api/curriculum/chapters/{chapterId}/lessons
   → Xem lessons trong chapter

4. GET /api/lessons/{lessonId}
   → Xem nội dung lesson

5. POST /api/lessons/{lessonId}/progress?percent=90
   → Update progress (auto-complete tại 90%)

6. GET /api/lessons/{lessonId}/next
   → Lấy thông tin lesson tiếp theo

7. Navigate to next lesson hoặc show modal
```

---

### Flow 3: Quiz

```
1. User làm quiz → Chọn đáp án

2. POST /api/lessons/quiz/submit
   → Nộp bài quiz

3. Nhận kết quả:
   - passed: true → Unlock chapter mới
   - passed: false → Phải làm lại

4. GET /api/lessons/{quizId}/next
   - PASS: Return chapter mới
   - FAIL: Return null + message "Làm lại"
```

---

## 🎨 UI COMPONENTS SUGGESTIONS

### 1. Course Card
```jsx
<CourseCard>
  <img src={thumbnailUrl} />
  <h3>{title}</h3>
  <div className="meta">
    <span>⭐ {rating}</span>
    <span>👥 {totalStudents}</span>
    <span>⏱️ {duration}h</span>
  </div>
  <div className="price">
    {discountPercent > 0 && (
      <span className="original">{price}đ</span>
    )}
    <span className="final">{discountedPrice}đ</span>
  </div>
</CourseCard>
```

### 2. Chapter List
```jsx
<ChapterList>
  {chapters.map(chapter => (
    <ChapterItem locked={!chapter.isUnlocked}>
      <h4>{chapter.title}</h4>
      <ProgressBar percent={chapter.progressPercent} />
      <p>{chapter.completedLessons}/{chapter.totalLessons}</p>
      
      {chapter.hasFinalQuiz && (
        <QuizStatus 
          passed={chapter.quizPassed}
          score={chapter.quizScore}
        />
      )}
      
      {!chapter.isUnlocked && (
        <LockOverlay>
          🔒 Hoàn thành Quiz Chương {chapter.order - 1}
        </LockOverlay>
      )}
    </ChapterItem>
  ))}
</ChapterList>
```

### 3. Next Lesson Modal
```jsx
<Modal show={showNextLesson}>
  <h2>✅ Hoàn thành bài học!</h2>
  <ProgressBar percent={totalProgress} />
  <p>{completedLessons}/{totalLessons} bài</p>
  
  {nextLesson && (
    <div className="next-lesson">
      <h3>Bài tiếp theo:</h3>
      <p>{nextLesson.title}</p>
      <span>⏱️ {nextLesson.duration} phút</span>
      <button onClick={() => navigate(`/lessons/${nextLesson.id}`)}>
        Tiếp tục học →
      </button>
    </div>
  )}
</Modal>
```

---

## ⚠️ IMPORTANT NOTES

### 1. Token Management
```javascript
// Check token trước mỗi request
const token = localStorage.getItem('token');
if (!token) {
  navigate('/login');
  return;
}

// Refresh token nếu hết hạn (implement nếu có API)
```

### 2. Error Handling
```javascript
const handleApiError = (error, response) => {
  if (response.status === 401) {
    // Token hết hạn
    localStorage.removeItem('token');
    navigate('/login');
  } else if (response.status === 403) {
    // Không có quyền
    showError('Bạn cần mua khóa học để xem nội dung này');
  } else {
    showError(error.message || 'Có lỗi xảy ra');
  }
};
```

### 3. Video Progress Tracking
```javascript
// Save progress mỗi 30 giây hoặc mỗi 10%
let lastSavedPercent = 0;

const saveProgress = async (percent) => {
  if (percent - lastSavedPercent >= 10) {
    await api.post(`/api/lessons/${lessonId}/progress?percent=${percent}`);
    lastSavedPercent = percent;
  }
};
```

---

## 🔧 TESTING

### Postman Testing

1. **Login first:**
   - Folder "1. AUTHENTICATION" → "Login (User)"
   - Token tự động lưu vào collection variables

2. **Test flow:**
   - Chạy từng folder theo thứ tự
   - Variables tự động update (courseId, chapterId, lessonId)

3. **Check responses:**
   - Tab "Test Results" hiển thị assertions
   - Console log hiển thị thông tin quan trọng

---

## 📚 DOCUMENTATION

Xem file **API_DOCUMENTATION_FOR_FRONTEND.md** để:
- Chi tiết từng API
- Request/Response format
- Frontend code examples
- UI suggestions
- Error handling

---

## 🎯 CHECKLIST

### Frontend cần implement:

- [ ] Authentication (Login/Register/Logout)
- [ ] Course listing & detail
- [ ] My Courses page
- [ ] Learning page (Chapters/Lessons)
- [ ] Video player với progress tracking
- [ ] Quiz interface
- [ ] Auto next lesson
- [ ] Chapter unlock logic UI
- [ ] Favorites
- [ ] Cart & Checkout
- [ ] User profile

---

## 🚀 READY TO START!

**Backend APIs:** ✅ Hoàn thành
**Documentation:** ✅ Đầy đủ
**Postman Collection:** ✅ Sẵn sàng
**Code Examples:** ✅ Có sẵn

**→ Frontend có thể bắt đầu implement ngay!** 🎉

---

## 📞 SUPPORT

Nếu có vấn đề với APIs:
1. Check Postman collection trước
2. Xem logs ở Backend console
3. Kiểm tra token có hợp lệ không
4. Verify request format theo documentation

---

**Happy Coding! 🚀**


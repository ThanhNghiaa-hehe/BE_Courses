# Frontend Guide - Video Progress Tracking với YouTube Embed

**Date:** November 23, 2025  
**Backend API Version:** 2.0  
**For:** Frontend Development Team

---

## 📋 Tổng quan

Hệ thống yêu cầu **track tiến độ xem video** của user để:
1. Unlock lesson tiếp theo khi xem >= 90% video
2. Theo dõi learning progress của user
3. Đảm bảo user xem đầy đủ nội dung trước khi chuyển bài

---

## 🎯 Yêu cầu chức năng

### **Logic unlock lesson:**
1. User phải xem **>= 90% video** của lesson hiện tại
2. Khi đạt 90% → Backend tự động mark lesson complete
3. Lesson tiếp theo sẽ được unlock
4. Nếu là lesson cuối chapter có quiz → Phải pass quiz mới unlock chapter mới

### **Video Progress API:**
- **Endpoint:** `POST /api/lessons/{lessonId}/progress?percent={percent}`
- **Auth:** Required (Bearer token)
- **Trigger:** Gọi API khi user đang xem video
- **Frequency:** Mỗi 5-10 giây (không nên quá thường xuyên)

---

## 🔌 Backend API Reference

### **1. Get Video Progress (NEW - Restore on reload)**

```http
GET /api/lessons/{lessonId}/progress
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Success",
  "data": {
    "lessonId": "lesson_id",
    "completed": false,
    "videoProgress": 45,        // ✅ Progress đã lưu
    "completedAt": null,
    "timeSpent": 120,
    "quizScore": null,
    "quizAttempts": 0,
    "quizPassedAt": null
  }
}

Response (chưa có progress):
{
  "success": true,
  "message": "Success",
  "data": {
    "lessonId": "lesson_id",
    "completed": false,
    "videoProgress": 0,         // Start from 0
    "completedAt": null,
    "timeSpent": 0,
    "quizScore": null,
    "quizAttempts": 0
  }
}

Response (chưa enroll khóa học):
{
  "success": false,
  "message": "User not enrolled in this course",
  "data": null
}
```

### **2. Update Video Progress**

```http
POST /api/lessons/{lessonId}/progress?percent=50
Authorization: Bearer {token}
Content-Type: application/json

Response 200 OK:
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "id": "progress_id",
    "userId": "user_id",
    "courseId": "course_id",
    "completedLessons": ["lesson1", "lesson2"],
    "currentLessonId": "lesson_id",
    "totalProgress": 45,
    "lessonsProgress": [
      {
        "lessonId": "lesson_id",
        "completed": false,
        "videoProgress": 50,
        "completedAt": null
      }
    ]
  }
}

Response khi >= 90%:
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    ...
    "lessonsProgress": [
      {
        "lessonId": "lesson_id",
        "completed": true,        // ✅ Auto completed
        "videoProgress": 90,
        "completedAt": "2025-11-23T10:30:00"
      }
    ]
  }
}
```

### **3. Check Lesson Access**

```http
GET /api/lessons/{lessonId}/access
Authorization: Bearer {token}

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

### **4. Get Next Lesson Info**

```http
GET /api/lessons/{lessonId}/next
Authorization: Bearer {token}

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
      "chapterId": "chapter_id",
      "chapterTitle": "React Hooks Advanced",
      "order": 2,
      "duration": 20,
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

**✅ NEW: Response includes `videoUrl`, `videoId`, and `videoThumbnail`**  
**Use case:** FE can directly switch to next video without calling GET /api/lessons/{id} again

---

## 💻 Implementation Guide

### **Option 1: React Player (Recommended - Dễ nhất)**

#### **1. Install Package**

```bash
npm install react-player
# hoặc
yarn add react-player
```

#### **2. Create VideoPlayer Component**

```jsx
// components/VideoPlayer.jsx
import React, { useRef, useState, useEffect } from 'react';
import ReactPlayer from 'react-player';
import axios from 'axios';

const VideoPlayer = ({ lessonId, videoUrl, onComplete }) => {
  const [progress, setProgress] = useState(0);
  const [isCompleted, setIsCompleted] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const lastProgressRef = useRef(0);
  const playerRef = useRef(null);

  // ✅ NEW: Load saved progress on mount
  useEffect(() => {
    loadSavedProgress();
  }, [lessonId]);

  const loadSavedProgress = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/lessons/${lessonId}/progress`,
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );

      if (response.data.success && response.data.data) {
        const savedProgress = response.data.data.videoProgress || 0;
        const isCompleted = response.data.data.completed || false;
        
        setProgress(savedProgress);
        setIsCompleted(isCompleted);
        lastProgressRef.current = savedProgress;

        console.log(`✅ Restored progress: ${savedProgress}%`);

        // Optional: Seek video to saved position
        if (playerRef.current && savedProgress > 0) {
          // Will be implemented when player is ready
        }
      }
    } catch (error) {
      console.error('❌ Failed to load progress:', error);
      // Start from 0 if error
      setProgress(0);
    } finally {
      setIsLoading(false);
    }
  };

  const updateProgressToBackend = async (percent) => {
    try {
      const token = localStorage.getItem('token'); // Hoặc từ Redux/Context
      
      const response = await axios.post(
        `http://localhost:8080/api/lessons/${lessonId}/progress`,
        null,
        {
          params: { percent },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      console.log(`✅ Updated progress: ${percent}%`);

      // Check if lesson auto completed (>= 90%)
      if (response.data.success && response.data.data) {
        const lessonProgress = response.data.data.lessonsProgress.find(
          lp => lp.lessonId === lessonId
        );
        
        if (lessonProgress && lessonProgress.completed && !isCompleted) {
          setIsCompleted(true);
          onComplete && onComplete();
          showCompletionNotification();
        }
      }
    } catch (error) {
      console.error('❌ Failed to update progress:', error);
      // Không throw error để không làm gián đoạn video
    }
  };

  const handleProgress = (state) => {
    const percent = Math.floor(state.played * 100);
    setProgress(percent);

    // Chỉ update khi thay đổi >= 5% để tránh spam API
    if (Math.abs(percent - lastProgressRef.current) >= 5) {
      lastProgressRef.current = percent;
      updateProgressToBackend(percent);
    }
  };

  const showCompletionNotification = () => {
    // Hiển thị notification
    alert('🎉 Chúc mừng! Bạn đã hoàn thành bài học này!');
    // Hoặc dùng toast notification library
  };

  if (isLoading) {
    return (
      <div className="video-player-loading">
        <p>Đang tải tiến độ...</p>
      </div>
    );
  }

  return (
    <div className="video-player-container">
      <ReactPlayer
        ref={playerRef}
        url={videoUrl}
        controls
        width="100%"
        height="500px"
        onProgress={handleProgress}
        progressInterval={5000} // Check mỗi 5 giây
        onReady={() => {
          // Seek to saved position when player ready
          if (playerRef.current && progress > 0 && !isCompleted) {
            const duration = playerRef.current.getDuration();
            const seekTo = (progress / 100) * duration;
            playerRef.current.seekTo(seekTo, 'seconds');
            console.log(`⏩ Seeked to ${progress}%`);
          }
        }}
        config={{
          youtube: {
            playerVars: {
              modestbranding: 1,
              rel: 0
            }
          }
        }}
      />
      
      {/* Progress Bar */}
      <div className="progress-bar">
        <div className="progress-fill" style={{ width: `${progress}%` }}>
          {progress}%
        </div>
      </div>

      {/* Completion Badge */}
      {isCompleted && (
        <div className="completion-badge">
          ✅ Đã hoàn thành
        </div>
      )}
    </div>
  );
};

export default VideoPlayer;
```

#### **3. Usage trong Lesson Page**

```jsx
// pages/LessonPage.jsx
import React, { useState, useEffect } from 'react';
import VideoPlayer from '../components/VideoPlayer';
import axios from 'axios';

const LessonPage = ({ lessonId }) => {
  const [lesson, setLesson] = useState(null);
  const [nextLesson, setNextLesson] = useState(null);
  const [showNextButton, setShowNextButton] = useState(false);

  useEffect(() => {
    fetchLesson();
  }, [lessonId]);

  const fetchLesson = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/lessons/${lessonId}`,
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      setLesson(response.data.data);
    } catch (error) {
      console.error('Failed to fetch lesson:', error);
    }
  };

  const handleLessonComplete = async () => {
    try {
      const token = localStorage.getItem('token');
      
      // Get next lesson info (includes videoUrl)
      const response = await axios.get(
        `http://localhost:8080/api/lessons/${lessonId}/next`,
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );

      if (response.data.success && response.data.data.nextLesson) {
        const nextLessonData = response.data.data.nextLesson;
        setNextLesson(nextLessonData);
        setShowNextButton(true);
        
        // ✅ NEW: videoUrl is included in response
        console.log('Next video URL:', nextLessonData.videoUrl);
      }
    } catch (error) {
      console.error('Failed to get next lesson:', error);
    }
  };

  const goToNextLesson = () => {
    if (nextLesson) {
      // Option 1: Navigate to next lesson page (recommended)
      window.location.href = `/lessons/${nextLesson.id}`;
      // Hoặc dùng React Router: navigate(`/lessons/${nextLesson.id}`);
      
      // Option 2: Switch video in current page (faster UX)
      // setLesson(nextLesson);
      // setLessonId(nextLesson.id);
    }
  };
  
  // ✅ NEW: Auto-switch video function
  const switchToNextVideo = () => {
    if (nextLesson && nextLesson.videoUrl) {
      // Update current lesson with next lesson data
      setLesson({
        id: nextLesson.id,
        title: nextLesson.title,
        description: nextLesson.description,
        videoUrl: nextLesson.videoUrl,
        duration: nextLesson.duration
      });
      setLessonId(nextLesson.id);
      setShowNextButton(false);
      
      console.log('✅ Switched to next video:', nextLesson.videoUrl);
    }
  };

  if (!lesson) return <div>Loading...</div>;

  return (
    <div className="lesson-page">
      <h1>{lesson.title}</h1>
      
      <VideoPlayer
        lessonId={lessonId}
        videoUrl={lesson.videoUrl}
        onComplete={handleLessonComplete}
      />

      <div className="lesson-description">
        <p>{lesson.description}</p>
      </div>

      {showNextButton && nextLesson && (
        <div className="next-lesson-section">
          <h3>🎯 Bài học tiếp theo</h3>
          <button onClick={goToNextLesson} className="next-lesson-btn">
            {nextLesson.title} →
          </button>
        </div>
      )}
    </div>
  );
};

export default LessonPage;
```

---

### **Option 2: YouTube IFrame API (Advanced - Nhiều control hơn)**

#### **1. Load YouTube API Script**

```jsx
// hooks/useYouTubePlayer.js
import { useEffect, useRef, useState } from 'react';

const useYouTubePlayer = (videoId, onProgressUpdate) => {
  const playerRef = useRef(null);
  const progressIntervalRef = useRef(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    // Load YouTube IFrame API
    if (!window.YT) {
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      const firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
    }

    window.onYouTubeIframeAPIReady = initializePlayer;

    return () => {
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
      }
      if (playerRef.current) {
        playerRef.current.destroy();
      }
    };
  }, [videoId]);

  const initializePlayer = () => {
    playerRef.current = new window.YT.Player('youtube-player', {
      videoId: videoId,
      playerVars: {
        autoplay: 0,
        controls: 1,
        modestbranding: 1,
        rel: 0
      },
      events: {
        onReady: (event) => {
          setIsReady(true);
          console.log('✅ YouTube player ready');
        },
        onStateChange: handleStateChange
      }
    });
  };

  const handleStateChange = (event) => {
    // YT.PlayerState.PLAYING = 1
    if (event.data === 1) {
      startProgressTracking();
    } else {
      stopProgressTracking();
    }
  };

  const startProgressTracking = () => {
    progressIntervalRef.current = setInterval(() => {
      const player = playerRef.current;
      if (player && player.getCurrentTime && player.getDuration) {
        const currentTime = player.getCurrentTime();
        const duration = player.getDuration();
        const percent = Math.floor((currentTime / duration) * 100);

        onProgressUpdate(percent);
      }
    }, 5000); // Mỗi 5 giây
  };

  const stopProgressTracking = () => {
    if (progressIntervalRef.current) {
      clearInterval(progressIntervalRef.current);
    }
  };

  return { isReady, player: playerRef.current };
};

export default useYouTubePlayer;
```

#### **2. Video Player Component**

```jsx
// components/YouTubePlayer.jsx
import React, { useState } from 'react';
import useYouTubePlayer from '../hooks/useYouTubePlayer';
import axios from 'axios';

const YouTubePlayer = ({ lessonId, videoId, onComplete }) => {
  const [progress, setProgress] = useState(0);
  const [lastSentProgress, setLastSentProgress] = useState(0);

  const handleProgressUpdate = async (percent) => {
    setProgress(percent);

    // Chỉ gửi khi thay đổi >= 5%
    if (Math.abs(percent - lastSentProgress) >= 5) {
      setLastSentProgress(percent);

      try {
        const token = localStorage.getItem('token');
        const response = await axios.post(
          `http://localhost:8080/api/lessons/${lessonId}/progress`,
          null,
          {
            params: { percent },
            headers: { 'Authorization': `Bearer ${token}` }
          }
        );

        console.log(`✅ Progress updated: ${percent}%`);

        // Check completion
        if (response.data.data.lessonsProgress) {
          const lessonProgress = response.data.data.lessonsProgress.find(
            lp => lp.lessonId === lessonId
          );
          
          if (lessonProgress && lessonProgress.completed) {
            onComplete && onComplete();
          }
        }
      } catch (error) {
        console.error('❌ Failed to update progress:', error);
      }
    }
  };

  const { isReady } = useYouTubePlayer(videoId, handleProgressUpdate);

  return (
    <div className="youtube-player-container">
      <div id="youtube-player"></div>
      
      {!isReady && (
        <div className="loading">Loading player...</div>
      )}

      <div className="progress-info">
        <span>Progress: {progress}%</span>
      </div>
    </div>
  );
};

export default YouTubePlayer;
```

---

## 🎨 UI/UX Recommendations

### **1. Progress Indicator**

```jsx
// components/ProgressBar.jsx
const ProgressBar = ({ progress, threshold = 90 }) => {
  const isComplete = progress >= threshold;

  return (
    <div className="progress-bar-wrapper">
      <div className="progress-bar">
        <div 
          className={`progress-fill ${isComplete ? 'complete' : ''}`}
          style={{ width: `${progress}%` }}
        >
          <span className="progress-text">{progress}%</span>
        </div>
      </div>
      
      <div className="progress-status">
        {isComplete ? (
          <span className="complete-badge">✅ Hoàn thành</span>
        ) : (
          <span className="incomplete-text">
            Cần xem {threshold - progress}% nữa để mở bài tiếp theo
          </span>
        )}
      </div>
    </div>
  );
};
```

### **2. Completion Modal**

```jsx
// components/CompletionModal.jsx
const CompletionModal = ({ show, nextLesson, onClose, onNext }) => {
  if (!show) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>🎉 Chúc mừng!</h2>
        </div>
        
        <div className="modal-body">
          <p>Bạn đã hoàn thành bài học này!</p>
          
          {nextLesson ? (
            <>
              <p>Bài học tiếp theo:</p>
              <h3>{nextLesson.title}</h3>
              <button onClick={onNext} className="btn-primary">
                Tiếp tục học →
              </button>
            </>
          ) : (
            <p>Bạn đã hoàn thành chương này! 🎓</p>
          )}
        </div>

        <button onClick={onClose} className="btn-close">
          Ở lại trang này
        </button>
      </div>
    </div>
  );
};
```

### **3. Locked Lesson Indicator**

```jsx
// components/LockedLesson.jsx
const LockedLesson = ({ lesson, requiredLesson }) => {
  return (
    <div className="locked-lesson">
      <div className="lock-icon">🔒</div>
      <h3>{lesson.title}</h3>
      <p className="lock-message">
        Bạn cần hoàn thành bài "{requiredLesson.title}" 
        và xem ít nhất 90% video để mở khóa bài này
      </p>
      <button className="btn-goto" onClick={() => goToLesson(requiredLesson.id)}>
        Đi đến bài yêu cầu
      </button>
    </div>
  );
};
```

---

## ⚠️ Important Notes

### **1. Error Handling**

```javascript
const updateProgressToBackend = async (percent) => {
  try {
    const response = await axios.post(
      `http://localhost:8080/api/lessons/${lessonId}/progress`,
      null,
      { params: { percent }, headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      // Token expired
      console.error('Token expired, redirecting to login...');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Access denied
      console.error('Access denied:', error.response.data.message);
      alert(error.response.data.message);
    } else {
      // Network error - retry sau 5s
      console.error('Network error, will retry...');
      setTimeout(() => updateProgressToBackend(percent), 5000);
    }
  }
};
```

### **2. Debouncing API Calls**

```javascript
import { debounce } from 'lodash';

// Tạo debounced function
const debouncedUpdate = useRef(
  debounce((lessonId, percent) => {
    updateProgressToBackend(lessonId, percent);
  }, 2000) // Chờ 2s sau lần gọi cuối
).current;

const handleProgress = (state) => {
  const percent = Math.floor(state.played * 100);
  debouncedUpdate(lessonId, percent);
};
```

### **3. Persist Progress - Database vs LocalStorage**

**❌ KHÔNG NÊN chỉ dùng localStorage:**
```javascript
// ❌ BAD: localStorage bị mất khi clear cache/đổi browser
localStorage.setItem(`lesson_${lessonId}_progress`, progress);
```

**✅ NÊN dùng Database (Backend API):**
```javascript
// ✅ GOOD: Progress được lưu vào MongoDB
// Step 1: Load progress from backend on mount
useEffect(() => {
  loadProgressFromBackend();
}, [lessonId]);

const loadProgressFromBackend = async () => {
  try {
    const response = await axios.get(
      `http://localhost:8080/api/lessons/${lessonId}/progress`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    
    if (response.data.success) {
      const savedProgress = response.data.data.videoProgress || 0;
      setProgress(savedProgress);
      
      // Optional: Use localStorage as cache
      localStorage.setItem(`lesson_${lessonId}_cache`, savedProgress);
    }
  } catch (error) {
    // Fallback to localStorage cache if API fails
    const cachedProgress = localStorage.getItem(`lesson_${lessonId}_cache`);
    if (cachedProgress) {
      setProgress(parseInt(cachedProgress));
    }
  }
};

// Step 2: Update backend (đã có trong updateProgressToBackend)
// Step 3: LocalStorage chỉ làm cache (optional)
```

**Lợi ích của Database:**
- ✅ Progress không mất khi clear cache
- ✅ Sync across devices (cùng account)
- ✅ Admin có thể xem progress của users
- ✅ Backend validate unlock logic
- ✅ Backup và recovery

### **4. Handle Video Seek (Skip)**

```javascript
// Với React Player
const [lastPosition, setLastPosition] = useState(0);

const handleProgress = (state) => {
  const currentPosition = state.playedSeconds;
  
  // Detect skip forward > 30 seconds
  if (currentPosition - lastPosition > 30) {
    console.warn('User skipped video');
    // Có thể hiển thị warning hoặc không update progress
  }
  
  setLastPosition(currentPosition);
  
  const percent = Math.floor(state.played * 100);
  updateProgressToBackend(percent);
};
```

---

## 🧪 Testing Guide

### **Test Cases:**

#### **1. Normal Video Watching**
```
✅ Test: Xem video từ đầu đến cuối
- Progress tăng dần: 0% → 10% → 20% → ... → 90%
- Khi đạt 90% → API trả về completed: true
- Hiển thị notification "Bài học hoàn thành"
```

#### **2. Video Pause/Resume**
```
✅ Test: Pause video giữa chừng
- Progress tracking dừng khi pause
- Resume → Progress tiếp tục từ vị trí cũ
- API vẫn được gọi đúng
```

#### **3. Page Reload**
```
✅ Test: Reload page khi đang xem video
- Progress được khôi phục từ backend
- Video tiếp tục từ vị trí đã lưu (optional)
```

#### **4. Skip Video**
```
⚠️ Test: User skip video forward
- Backend có thể reject nếu skip quá nhiều
- Hiển thị message từ backend
```

#### **5. Multiple Lessons**
```
✅ Test: Xem nhiều lessons liên tiếp
- Progress của mỗi lesson được track riêng
- Lesson tiếp theo unlock đúng lúc
```

#### **6. Network Error**
```
✅ Test: Mất kết nối internet
- Progress được cache local
- Retry khi có mạng lại
```

---

## 📊 API Response Examples

### **Success - Video đang xem (< 90%)**

```json
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "lessonsProgress": [
      {
        "lessonId": "lesson123",
        "completed": false,
        "videoProgress": 45,
        "completedAt": null
      }
    ]
  }
}
```

### **Success - Video completed (>= 90%)**

```json
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "completedLessons": ["lesson123"],
    "lessonsProgress": [
      {
        "lessonId": "lesson123",
        "completed": true,
        "videoProgress": 95,
        "completedAt": "2025-11-23T15:30:00"
      }
    ]
  }
}
```

### **Error - User chưa enroll**

```json
{
  "success": false,
  "message": "Progress not found",
  "data": null
}
```

### **Error - Skip quá nhiều (nếu BE validate)**

```json
{
  "success": false,
  "message": "Không thể bỏ qua quá nhiều nội dung video. Vui lòng xem video đầy đủ.",
  "data": null
}
```

---

## 🔗 Additional Resources

### **Libraries:**
- React Player: https://www.npmjs.com/package/react-player
- YouTube IFrame API: https://developers.google.com/youtube/iframe_api_reference
- Axios: https://axios-http.com/

### **Example Repos:**
- React Video Player: https://github.com/cookpete/react-player
- YouTube API Examples: https://github.com/youtube/api-samples

### **Contact Backend Team:**
- API Issues: Create ticket in Jira
- Questions: Slack #backend-support
- Documentation: See `ALL_ENDPOINTS.md`

---

## ✅ Checklist cho FE Team

- [ ] Install React Player hoặc setup YouTube IFrame API
- [ ] Implement VideoPlayer component
- [ ] Integrate với API `/api/lessons/{id}/progress`
- [ ] Handle completion state (>= 90%)
- [ ] Show next lesson button khi complete
- [ ] Handle locked lessons (< 90% previous lesson)
- [ ] Implement error handling
- [ ] Add progress persistence (localStorage)
- [ ] Test all scenarios
- [ ] UI/UX cho progress bar và completion state

---

**Last Updated:** November 23, 2025  
**Backend API Version:** 2.0  
**Questions?** Contact Backend Team hoặc xem `ALL_ENDPOINTS.md`


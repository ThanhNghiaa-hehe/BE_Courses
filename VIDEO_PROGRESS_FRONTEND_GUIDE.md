# 🎥 VIDEO PROGRESS TRACKING - HƯỚNG DẪN CHO FRONTEND

**Ngày:** 23/11/2025  
**Mục đích:** Hướng dẫn Frontend implement video progress tracking với YouTube embed

---

## 📋 MỤC LỤC

1. [Tổng quan](#tổng-quan)
2. [API Backend](#api-backend)
3. [YouTube iframe API](#youtube-iframe-api)
4. [Implementation Guide](#implementation-guide)
5. [Complete Example](#complete-example)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 TỔNG QUAN

### **Cách hoạt động:**

```
User xem video → FE track progress → Call BE API → BE lưu progress
→ Auto complete khi >= 90% → Unlock lesson tiếp theo
```

### **Điểm quan trọng:**

⚠️ **Backend KHÔNG tự động track progress!**  
✅ **Frontend PHẢI gọi API để update progress!**

YouTube video embed không tự động gửi progress về server. FE phải:
1. Dùng YouTube iframe API để track video
2. Tính % progress (currentTime / duration)
3. Gọi BE API định kỳ hoặc khi pause/end

---

## 📡 API BACKEND

### **1. Update Video Progress**

```http
POST /api/lessons/{lessonId}/progress?percent={percent}
Authorization: Bearer {token}
```

**Request:**
```
POST http://localhost:8080/api/lessons/673abc123/progress?percent=75
Authorization: Bearer eyJhbGc...
```

**Response:**
```json
{
  "success": true,
  "message": "Video progress updated",
  "data": {
    "userId": "user123",
    "courseId": "course456",
    "lessonProgress": [
      {
        "lessonId": "673abc123",
        "videoProgress": 75,
        "completed": false
      }
    ]
  }
}
```

### **2. Backend Auto-Complete Logic**

```java
if (percent >= 90 && !lessonProgress.getCompleted()) {
    lessonProgress.setCompleted(true);
    lessonProgress.setCompletedAt(LocalDateTime.now());
    progress.markLessonComplete(lessonId);
    updateTotalProgress(progress, courseId);
}
```

**Khi percent >= 90%:**
- ✅ Lesson tự động mark complete
- ✅ Unlock lesson tiếp theo
- ✅ Update total course progress
- ✅ Update lastAccessedAt

---

## 🎬 YOUTUBE IFRAME API

### **1. Load YouTube iframe API**

Thêm vào HTML:
```html
<script src="https://www.youtube.com/iframe_api"></script>
```

### **2. Player States**

```javascript
YT.PlayerState.UNSTARTED   // -1
YT.PlayerState.ENDED       //  0
YT.PlayerState.PLAYING     //  1
YT.PlayerState.PAUSED      //  2
YT.PlayerState.BUFFERING   //  3
YT.PlayerState.CUED        //  5
```

### **3. Player Methods**

```javascript
player.getCurrentTime()   // Thời gian hiện tại (seconds)
player.getDuration()      // Tổng thời lượng (seconds)
player.getPlayerState()   // Trạng thái hiện tại
player.pauseVideo()       // Pause
player.playVideo()        // Play
```

---

## 💻 IMPLEMENTATION GUIDE

### **BƯỚC 1: Setup HTML**

```html
<!-- Video Container -->
<div class="video-container">
  <div id="youtube-player"></div>
  <div class="video-progress-bar">
    <div class="progress" id="local-progress" style="width: 0%"></div>
  </div>
  <div class="video-info">
    <span id="current-time">0:00</span> / 
    <span id="duration">0:00</span>
    <span id="percent">0%</span>
  </div>
</div>
```

### **BƯỚC 2: Initialize YouTube Player**

```javascript
let player;
let trackInterval;
let lessonId = "lesson-id-from-url";
let token = "user-jwt-token";

// YouTube API ready callback
function onYouTubeIframeAPIReady() {
  player = new YT.Player('youtube-player', {
    height: '480',
    width: '854',
    videoId: getVideoIdFromUrl(videoUrl), // Extract từ YouTube URL
    playerVars: {
      'autoplay': 0,
      'controls': 1,
      'modestbranding': 1,
      'rel': 0
    },
    events: {
      'onReady': onPlayerReady,
      'onStateChange': onPlayerStateChange
    }
  });
}

// Extract video ID từ URL
function getVideoIdFromUrl(url) {
  // URL format: https://www.youtube.com/watch?v=VIDEO_ID
  const regex = /[?&]v=([^&]+)/;
  const match = url.match(regex);
  return match ? match[1] : null;
}
```

### **BƯỚC 3: Handle Player Events**

```javascript
function onPlayerReady(event) {
  console.log('Player ready');
  // Load progress đã lưu (nếu có)
  loadSavedProgress();
}

function onPlayerStateChange(event) {
  switch(event.data) {
    case YT.PlayerState.PLAYING:
      console.log('Video playing');
      startTracking();
      break;
      
    case YT.PlayerState.PAUSED:
      console.log('Video paused');
      stopTracking();
      updateProgress(); // Save ngay khi pause
      break;
      
    case YT.PlayerState.ENDED:
      console.log('Video ended');
      stopTracking();
      updateProgress(); // Save khi kết thúc
      markComplete(); // Mark 100%
      break;
  }
}
```

### **BƯỚC 4: Track Progress**

```javascript
function startTracking() {
  // Update UI mỗi 1 giây
  trackInterval = setInterval(() => {
    updateUI();
  }, 1000);
  
  // Gọi BE API mỗi 10 giây
  saveInterval = setInterval(() => {
    updateProgress();
  }, 10000);
}

function stopTracking() {
  if (trackInterval) {
    clearInterval(trackInterval);
    trackInterval = null;
  }
  if (saveInterval) {
    clearInterval(saveInterval);
    saveInterval = null;
  }
}

function updateUI() {
  if (!player || !player.getCurrentTime) return;
  
  const currentTime = player.getCurrentTime();
  const duration = player.getDuration();
  const percent = Math.floor((currentTime / duration) * 100);
  
  // Update UI
  document.getElementById('current-time').textContent = formatTime(currentTime);
  document.getElementById('duration').textContent = formatTime(duration);
  document.getElementById('percent').textContent = percent + '%';
  document.getElementById('local-progress').style.width = percent + '%';
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}
```

### **BƯỚC 5: Call Backend API**

```javascript
async function updateProgress() {
  if (!player || !player.getCurrentTime) return;
  
  const currentTime = player.getCurrentTime();
  const duration = player.getDuration();
  const percent = Math.floor((currentTime / duration) * 100);
  
  // Skip nếu percent quá nhỏ
  if (percent < 1) return;
  
  try {
    const response = await fetch(
      `http://localhost:8080/api/lessons/${lessonId}/progress?percent=${percent}`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const data = await response.json();
    
    if (data.success) {
      console.log('Progress saved:', percent + '%');
      
      // Check nếu auto-completed (>= 90%)
      const lessonProgress = data.data.lessonProgress.find(
        lp => lp.lessonId === lessonId
      );
      
      if (lessonProgress && lessonProgress.completed) {
        onLessonCompleted();
      }
    } else {
      console.error('Failed to save progress:', data.message);
    }
  } catch (error) {
    console.error('Error updating progress:', error);
  }
}

async function markComplete() {
  // Force 100% khi video ended
  try {
    const response = await fetch(
      `http://localhost:8080/api/lessons/${lessonId}/progress?percent=100`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const data = await response.json();
    if (data.success) {
      onLessonCompleted();
    }
  } catch (error) {
    console.error('Error marking complete:', error);
  }
}
```

### **BƯỚC 6: Handle Completion**

```javascript
function onLessonCompleted() {
  console.log('Lesson completed!');
  
  // Show success message
  showSuccessModal('Hoàn thành bài học!');
  
  // Get next lesson
  getNextLesson();
}

async function getNextLesson() {
  try {
    const response = await fetch(
      `http://localhost:8080/api/lessons/${lessonId}/next`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    const data = await response.json();
    
    if (data.success && data.data) {
      const nextLesson = data.data.nextLesson;
      
      if (nextLesson) {
        // Show "Next Lesson" button
        showNextLessonButton(nextLesson);
      } else {
        // No more lessons, maybe quiz
        if (data.data.hasQuiz) {
          showQuizButton(data.data.quizId);
        } else {
          showCourseCompleteMessage();
        }
      }
    }
  } catch (error) {
    console.error('Error getting next lesson:', error);
  }
}

function showNextLessonButton(lesson) {
  const html = `
    <div class="next-lesson-card">
      <h3>Bài tiếp theo</h3>
      <p>${lesson.title}</p>
      <button onclick="goToLesson('${lesson.id}')">
        Tiếp tục học →
      </button>
    </div>
  `;
  document.getElementById('next-lesson-container').innerHTML = html;
}

function goToLesson(lessonId) {
  window.location.href = `/learn/lessons/${lessonId}`;
}
```

### **BƯỚC 7: Load Saved Progress**

```javascript
async function loadSavedProgress() {
  try {
    const response = await fetch(
      `http://localhost:8080/api/lessons/${lessonId}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    const data = await response.json();
    
    if (data.success && data.data.videoProgress) {
      const savedPercent = data.data.videoProgress;
      const duration = player.getDuration();
      const startTime = (savedPercent / 100) * duration;
      
      // Seek to saved position
      player.seekTo(startTime, true);
      
      console.log(`Resumed from ${savedPercent}%`);
    }
  } catch (error) {
    console.error('Error loading progress:', error);
  }
}
```

---

## 📦 COMPLETE EXAMPLE

### **React Component**

```jsx
import React, { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';

const VideoLesson = () => {
  const { lessonId } = useParams();
  const playerRef = useRef(null);
  const [progress, setProgress] = useState(0);
  const [completed, setCompleted] = useState(false);
  const token = localStorage.getItem('token');
  
  useEffect(() => {
    // Load YouTube API
    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';
    document.body.appendChild(tag);
    
    window.onYouTubeIframeAPIReady = initPlayer;
    
    return () => {
      if (playerRef.current) {
        playerRef.current.destroy();
      }
    };
  }, [lessonId]);
  
  const initPlayer = () => {
    playerRef.current = new window.YT.Player('youtube-player', {
      videoId: getVideoId(),
      events: {
        'onReady': handlePlayerReady,
        'onStateChange': handleStateChange
      }
    });
  };
  
  const handlePlayerReady = () => {
    loadSavedProgress();
  };
  
  const handleStateChange = (event) => {
    if (event.data === window.YT.PlayerState.PLAYING) {
      startTracking();
    } else if (event.data === window.YT.PlayerState.PAUSED) {
      stopTracking();
      saveProgress();
    } else if (event.data === window.YT.PlayerState.ENDED) {
      stopTracking();
      markComplete();
    }
  };
  
  const startTracking = () => {
    const interval = setInterval(() => {
      updateProgress();
    }, 10000);
    return interval;
  };
  
  const updateProgress = async () => {
    const current = playerRef.current.getCurrentTime();
    const duration = playerRef.current.getDuration();
    const percent = Math.floor((current / duration) * 100);
    
    setProgress(percent);
    
    // Save to backend
    await saveProgress(percent);
  };
  
  const saveProgress = async (percent) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/lessons/${lessonId}/progress?percent=${percent}`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      const data = await response.json();
      
      if (data.success) {
        const lessonProgress = data.data.lessonProgress.find(
          lp => lp.lessonId === lessonId
        );
        
        if (lessonProgress?.completed) {
          setCompleted(true);
        }
      }
    } catch (error) {
      console.error('Error saving progress:', error);
    }
  };
  
  return (
    <div className="video-lesson">
      <div id="youtube-player"></div>
      <div className="progress-bar">
        <div 
          className="progress" 
          style={{ width: `${progress}%` }}
        />
      </div>
      <p>Progress: {progress}%</p>
      
      {completed && (
        <div className="completion-badge">
          ✅ Đã hoàn thành!
        </div>
      )}
    </div>
  );
};

export default VideoLesson;
```

---

## 🎯 BEST PRACTICES

### **1. Tần suất update:**

```javascript
// ✅ GOOD: Update UI mỗi 1s, save BE mỗi 10s
setInterval(updateUI, 1000);
setInterval(saveProgress, 10000);

// ❌ BAD: Save BE quá thường xuyên
setInterval(saveProgress, 1000); // Tốn tài nguyên!
```

### **2. Error handling:**

```javascript
try {
  await saveProgress(percent);
} catch (error) {
  console.error('Error:', error);
  // Retry sau 5s
  setTimeout(() => saveProgress(percent), 5000);
}
```

### **3. Cleanup:**

```javascript
useEffect(() => {
  const interval = startTracking();
  
  return () => {
    clearInterval(interval); // Cleanup khi unmount
    saveProgress(); // Save lần cuối
  };
}, []);
```

### **4. Network optimization:**

```javascript
// Debounce save requests
let saveTimeout;
function debouncedSave(percent) {
  clearTimeout(saveTimeout);
  saveTimeout = setTimeout(() => {
    saveProgress(percent);
  }, 2000);
}
```

### **5. User experience:**

```javascript
// Show loading khi seeking
player.addEventListener('seeking', () => {
  showLoadingSpinner();
});

player.addEventListener('seeked', () => {
  hideLoadingSpinner();
});

// Auto-pause khi user leaves tab
document.addEventListener('visibilitychange', () => {
  if (document.hidden) {
    player.pauseVideo();
  }
});
```

---

## 🐛 TROUBLESHOOTING

### **Problem 1: Progress không được lưu**

**Nguyên nhân:**
- Token hết hạn
- lessonId sai
- API call fail

**Giải pháp:**
```javascript
// Check token trước khi call
if (!token || isTokenExpired(token)) {
  redirectToLogin();
  return;
}

// Validate lessonId
if (!lessonId || lessonId === 'undefined') {
  console.error('Invalid lessonId');
  return;
}

// Log API errors
console.log('Saving progress:', { lessonId, percent });
```

### **Problem 2: Video không auto-complete**

**Nguyên nhân:**
- Percent chưa đến 90%
- API không được gọi khi ended

**Giải pháp:**
```javascript
// Force 100% khi video ended
if (event.data === YT.PlayerState.ENDED) {
  await saveProgress(100); // Đảm bảo >= 90%
}
```

### **Problem 3: Progress bar không sync**

**Nguyên nhân:**
- Update UI và save BE không đồng bộ

**Giải pháp:**
```javascript
// Update UI ngay lập tức
function updateUI() {
  const percent = calculatePercent();
  setProgress(percent); // Update state ngay
}

// Save BE bất đồng bộ
async function saveProgress() {
  const percent = calculatePercent();
  await callAPI(percent); // Không block UI
}
```

### **Problem 4: Multiple API calls**

**Nguyên nhân:**
- User click play/pause nhiều lần

**Giải pháp:**
```javascript
let isSaving = false;

async function saveProgress() {
  if (isSaving) return; // Skip nếu đang save
  
  isSaving = true;
  try {
    await callAPI();
  } finally {
    isSaving = false;
  }
}
```

---

## 📊 TESTING CHECKLIST

### **Scenario 1: First time watch**

- [ ] Video load thành công
- [ ] Progress bar hiển thị 0%
- [ ] Play video → Progress bắt đầu tăng
- [ ] Pause → Progress được save
- [ ] Resume → Tiếp tục từ vị trí đã save

### **Scenario 2: Resume watching**

- [ ] Load saved progress từ BE
- [ ] Video seek đến vị trí đã save
- [ ] Progress bar hiển thị % đúng
- [ ] Continue playing → Progress tăng từ vị trí cũ

### **Scenario 3: Complete lesson**

- [ ] Watch đến >= 90%
- [ ] Backend auto-complete
- [ ] UI hiển thị "Hoàn thành"
- [ ] Show next lesson button
- [ ] Click next → Navigate đúng

### **Scenario 4: Error handling**

- [ ] Network error → Retry
- [ ] Token expired → Redirect login
- [ ] Invalid lessonId → Show error
- [ ] API fail → Show message

---

## 🎓 SUMMARY

### **Flow tổng quát:**

```
1. Load YouTube iframe API
2. Initialize player với videoId
3. Listen to player state changes
4. Track progress mỗi giây (UI)
5. Save progress mỗi 10s (BE)
6. Auto-complete khi >= 90%
7. Show next lesson
```

### **Key points:**

- ✅ FE responsible for tracking
- ✅ Call BE API to save progress
- ✅ BE auto-complete >= 90%
- ✅ Update UI real-time
- ✅ Handle errors gracefully
- ✅ Optimize network calls

### **APIs cần dùng:**

```
POST /api/lessons/{id}/progress?percent={percent}  // Save progress
GET  /api/lessons/{id}                             // Get lesson + saved progress
GET  /api/lessons/{id}/next                        // Get next lesson
POST /api/lessons/{id}/complete                    // Manual complete (optional)
```

---

## 📞 SUPPORT

**Nếu có vấn đề:**

1. Check browser console for errors
2. Check network tab for API calls
3. Verify token in localStorage
4. Test với Postman trước
5. Contact backend team

**Backend team:**
- API docs: `/api-docs`
- Contact: backend-team@example.com

---

**Happy Coding!** 🚀

**Last updated:** November 23, 2025


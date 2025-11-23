# Bug Fix Report - Video Progress & Quiz User ID Issue

**Date:** November 23, 2025  
**Severity:** 🔴 CRITICAL  
**Status:** ✅ FIXED

---

## 🐛 Bug Description

### Problem
Các endpoints liên quan đến video progress, lesson tracking, và quiz đang sử dụng **hardcoded userId** là `"temp-user-id"` thay vì lấy **userId thật** từ JWT authentication token.

### Impact
- ❌ **Video progress không được lưu đúng user**
- ❌ **Lesson completion tracking sai**
- ❌ **Quiz results không liên kết với user đúng**
- ❌ **Access control không hoạt động**
- ❌ **Multi-user system hoàn toàn bị hỏng**

### Affected Endpoints

#### LessonUserController (`/api/lessons`)
1. ❌ `POST /{id}/complete` - Mark lesson complete
2. ❌ `POST /{id}/progress` - Update video progress **[CRITICAL]**
3. ❌ `GET /{id}/access` - Check lesson access
4. ❌ `GET /{id}/next` - Get next lesson info
5. ❌ `GET /{id}` - Get lesson (access check disabled)

#### QuizUserController (`/api/quizzes`)
1. ❌ `POST /submit` - Submit quiz
2. ❌ `GET /{quizId}/attempts` - Get quiz attempts
3. ❌ `GET /{quizId}/passed` - Check if passed

---

## 🔧 Root Cause

### Before (Bug)
```java
@PostMapping("/{id}/progress")
public ResponseEntity<ResponseMessage<UserProgress>> updateVideoProgress(
        @PathVariable String id,
        @RequestParam Integer percent,
        Authentication authentication
) {
    String userEmail = authentication.getName();
    String userId = "temp-user-id";  // ❌ HARDCODED!
    
    return ResponseEntity.ok(progressService.updateVideoProgress(userId, id, percent));
}
```

**Problem:**
- Authentication object có sẵn nhưng không được sử dụng
- Sử dụng hardcoded `"temp-user-id"`
- Tất cả users đều có cùng progress vì dùng chung userId

---

## ✅ Solution

### After (Fixed)

#### 1. Add UserRepository dependency
```java
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonUserController {

    private final LessonService lessonService;
    private final ProgressService progressService;
    private final UserRepository userRepository;  // ✅ Added
}
```

#### 2. Create helper method to get real userId
```java
/**
 * Helper method to get userId from Authentication
 */
private String getUserId(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
}
```

#### 3. Use helper method in all endpoints
```java
@PostMapping("/{id}/progress")
public ResponseEntity<ResponseMessage<UserProgress>> updateVideoProgress(
        @PathVariable String id,
        @RequestParam Integer percent,
        Authentication authentication
) {
    String userId = getUserId(authentication);  // ✅ Get real userId
    return ResponseEntity.ok(progressService.updateVideoProgress(userId, id, percent));
}
```

---

## 📝 Changes Made

### File 1: `LessonUserController.java`

**Changes:**
1. ✅ Added `UserRepository` dependency
2. ✅ Added `getUserId(Authentication)` helper method
3. ✅ Fixed `getLesson()` - Now checks access with real userId
4. ✅ Fixed `markLessonComplete()` - Uses real userId
5. ✅ Fixed `updateVideoProgress()` - Uses real userId **[CRITICAL FIX]**
6. ✅ Fixed `checkAccess()` - Uses real userId
7. ✅ Fixed `getNextLessonInfo()` - Uses real userId

**Total fixes:** 6 methods + 1 access control enabled

### File 2: `QuizUserController.java`

**Changes:**
1. ✅ Added `UserRepository` dependency
2. ✅ Added `getUserId(Authentication)` helper method
3. ✅ Fixed `submitQuiz()` - Uses real userId
4. ✅ Fixed `getAttempts()` - Uses real userId
5. ✅ Fixed `hasPassedQuiz()` - Uses real userId

**Total fixes:** 3 methods

---

## 🎯 Testing Checklist

### Manual Testing

#### Video Progress
- [ ] Test update video progress
  ```bash
  POST /api/lessons/{lessonId}/progress?percent=50
  Authorization: Bearer {user_token}
  ```
- [ ] Verify progress saved for correct user in database
- [ ] Test with different users - each should have separate progress

#### Lesson Completion
- [ ] Test mark lesson complete
  ```bash
  POST /api/lessons/{lessonId}/complete
  Authorization: Bearer {user_token}
  ```
- [ ] Verify completion saved for correct user
- [ ] Test next lesson info after completion

#### Quiz
- [ ] Test submit quiz
  ```bash
  POST /api/quizzes/submit
  Body: { "quizId": "...", "answers": [...] }
  ```
- [ ] Verify quiz result linked to correct user
- [ ] Test get quiz attempts - should show correct user's history

#### Access Control
- [ ] Test lesson access with enrolled user - should succeed
- [ ] Test lesson access with non-enrolled user - should fail (403)
- [ ] Verify access control using real userId

### Database Verification

Check MongoDB collections:
```javascript
// UserProgress collection
db.userProgress.find({ userId: "real_user_id" })

// QuizAttempts collection
db.quizAttempts.find({ userId: "real_user_id" })

// Should NOT find any with "temp-user-id"
db.userProgress.find({ userId: "temp-user-id" })
db.quizAttempts.find({ userId: "temp-user-id" })
```

---

## 📊 Before vs After

| Feature | Before | After |
|---------|--------|-------|
| Video Progress | ❌ All users share same progress | ✅ Each user has own progress |
| Lesson Complete | ❌ Wrong user tracking | ✅ Correct user tracking |
| Quiz Results | ❌ Not linked to user | ✅ Properly linked to user |
| Access Control | ❌ Disabled/Broken | ✅ Working with real userId |
| Multi-user Support | ❌ Broken | ✅ Working |

---

## 🔒 Security Improvements

### Access Control Now Working
```java
@GetMapping("/{id}")
public ResponseEntity<ResponseMessage<Lesson>> getLesson(
        @PathVariable String id,
        Authentication authentication
) {
    String userId = getUserId(authentication);
    
    // ✅ NOW WORKING: Check if user has access to this lesson
    ResponseMessage<Boolean> access = progressService.canAccessLesson(userId, id);
    if (!Boolean.TRUE.equals(access.getData())) {
        return ResponseEntity.status(403)
            .body(new ResponseMessage<>(false, access.getMessage(), null));
    }

    return ResponseEntity.ok(lessonService.getLessonById(id));
}
```

**Benefits:**
- ✅ Users can only access lessons from courses they enrolled in
- ✅ Proper authorization checks
- ✅ 403 Forbidden for unauthorized access

---

## 🚀 Similar Pattern Used In

These controllers already use the correct pattern:
- ✅ `ProgressController` - Already correct from the start
- ✅ `PaymentController` - Uses UserRepository correctly
- ✅ `UserController` - Uses @AuthenticationPrincipal

**Pattern to follow:**
```java
@RestController
@RequiredArgsConstructor
public class SomeController {
    private final UserRepository userRepository;
    
    private String getUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    
    @PostMapping("/endpoint")
    public ResponseEntity<?> someEndpoint(Authentication authentication) {
        String userId = getUserId(authentication);
        // Use real userId...
    }
}
```

---

## 📋 Affected Data

### Data Created with Bug
If system was already in use, you may have data with `userId = "temp-user-id"`:

```javascript
// Clean up bad data in MongoDB
db.userProgress.deleteMany({ userId: "temp-user-id" })
db.quizAttempts.deleteMany({ userId: "temp-user-id" })
db.lessonProgress.deleteMany({ userId: "temp-user-id" })
```

### Migration (if needed)
If you need to migrate existing data, this is **NOT POSSIBLE** because we don't know which real user created which record when all have `"temp-user-id"`.

**Recommendation:** Delete all progress/quiz data and start fresh.

---

## 🔍 How to Find Similar Bugs

Search for these patterns:
```bash
# Search for temp-user-id
grep -r "temp-user-id" src/

# Search for TODO comments related to userId
grep -r "TODO.*userId" src/

# Search for authentication.getName() not being used
grep -r "authentication.getName()" src/ | grep "//"
```

---

## ✅ Verification

### Compile Check
```bash
mvn clean compile
```
**Result:** ✅ SUCCESS (only warnings about "never used" - normal for Spring controllers)

### No More Hardcoded IDs
```bash
grep -r "temp-user-id" src/
```
**Result:** ✅ No matches (all fixed)

---

## 📚 Documentation Updates

Updated files:
- ✅ `ALL_ENDPOINTS.md` - All endpoints documented with correct authentication
- ✅ `PAYMENT_API_GUIDE.md` - Payment flow with real user authentication
- ✅ `MIGRATION_NOTES.md` - Migration guide updated

---

## 🎓 Lessons Learned

1. **Never use hardcoded user IDs** - Always get from authentication
2. **Test with multiple users** - Would have caught this immediately
3. **Code review importance** - This should have been caught in review
4. **TODO comments** - Should be tracked and resolved before deployment
5. **Authentication pattern** - Use consistent helper method across all controllers

---

## 🔐 Best Practices Going Forward

### For New Controllers

```java
@RestController
@RequestMapping("/api/something")
@RequiredArgsConstructor
public class NewController {
    
    private final UserRepository userRepository;
    
    // ✅ ALWAYS include this helper method
    private String getUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    
    @PostMapping("/endpoint")
    public ResponseEntity<?> someEndpoint(Authentication authentication) {
        // ✅ ALWAYS get real userId
        String userId = getUserId(authentication);
        
        // ❌ NEVER do this
        // String userId = "temp-user-id";
        
        // Use userId...
    }
}
```

### Code Review Checklist
- [ ] No hardcoded user IDs
- [ ] Authentication object is used
- [ ] userId extracted from JWT token
- [ ] Access control checks in place
- [ ] No "temp" or "test" values in production code
- [ ] TODO comments resolved or tracked

---

## 📞 Contact

If you find similar issues or have questions:
- Create an issue in project repository
- Contact development team
- Review security practices

---

**Fix Status:** ✅ COMPLETED  
**Tested:** ⏳ PENDING  
**Deployed:** ⏳ PENDING  
**Priority:** 🔴 CRITICAL - Deploy immediately after testing


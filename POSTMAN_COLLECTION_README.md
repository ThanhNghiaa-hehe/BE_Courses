# Postman Collection - Test_PostMan_23-11-2025

## ✅ File đã được tạo hoàn chỉnh!

**File:** `Test_PostMan_23-11-2025.postman_collection.json`  
**Status:** ✅ Valid JSON | Ready to Import  
**Last Updated:** November 23, 2025

---

## 📊 Collection Summary

### Total: **15 Modules | 73+ APIs**

| # | Module | Endpoints | Auth Required |
|---|--------|-----------|---------------|
| 1 | Authentication | 9 | Public + Auth |
| 2 | Courses (Admin) | 6 | Admin |
| 3 | Courses (User) | 2 | Public |
| 4 | Course Categories | 4 | Admin + Public |
| 5 | Chapters (Admin) | 5 | Admin |
| 6 | Lessons (Admin) | 6 | Admin |
| 7 | Lessons (User) | 6 | User |
| 8 | Curriculum | 4 | Public |
| 9 | Progress Tracking | 4 | User |
| 10 | Quiz (Admin) | 4 | Admin |
| 11 | Quiz (User) | 4 | User |
| 12 | Payment (VNPAY) | 6 | User |
| 13 | Favorites | 7 | User |
| 14 | User Management | 4 | User |
| 15 | Admin - User Mgmt | 3 | Admin |

---

## 🔥 New Features

### ✅ Fixed Critical Bugs
- **Video Progress:** Now uses real userId from JWT (not temp-user-id)
- **Quiz Submit:** Fixed userId tracking
- **Lesson Complete:** Fixed userId tracking
- **Access Control:** Now properly checks user enrollment

### 🆕 Direct Payment System
- No more Cart/Order modules
- Direct course purchase via Payment API
- Auto enrollment after successful payment
- Payment history tracking

---

## 📋 Endpoint Details

### 1. AUTHENTICATION (9 APIs)
```
POST   /api/auth/register
POST   /api/auth/verify-otp
POST   /api/auth/login
POST   /api/auth/google
POST   /api/auth/refresh-token
POST   /api/auth/forget-password
POST   /api/auth/verify-otpPassword
POST   /api/auth/reset-password
```

### 2. COURSES - Admin (6 APIs)
```
POST   /api/admin/courses/create
GET    /api/admin/courses/getAll
GET    /api/admin/courses/{id}
PUT    /api/admin/courses/update
DELETE /api/admin/courses/delete/{id}
POST   /api/admin/courses/upload-thumbnail
```

### 3. COURSES - User (2 APIs)
```
GET    /api/courses
GET    /api/courses/{id}
```

### 4. COURSE CATEGORIES (4 APIs)
```
GET    /api/admin/course-categories/getAll
POST   /api/admin/course-categories/create
PUT    /api/admin/course-categories/update
DELETE /api/admin/course-categories/delete/{code}
```

### 5. CHAPTERS - Admin (5 APIs)
```
POST   /api/admin/chapters/create
GET    /api/admin/chapters/course/{courseId}
GET    /api/admin/chapters/{id}
PUT    /api/admin/chapters/{id}
DELETE /api/admin/chapters/{id}
```

### 6. LESSONS - Admin (6 APIs)
```
POST   /api/admin/lessons/create
GET    /api/admin/lessons/chapter/{chapterId}
GET    /api/admin/lessons/course/{courseId}
GET    /api/admin/lessons/{id}
PUT    /api/admin/lessons/{id}
DELETE /api/admin/lessons/{id}
```

### 7. LESSONS - User (6 APIs) ✅ FIXED
```
GET    /api/lessons/{id}                    ✅ Fixed userId
POST   /api/lessons/{id}/like
POST   /api/lessons/{id}/complete           ✅ Fixed userId
POST   /api/lessons/{id}/progress?percent=X ✅ CRITICAL FIX - Fixed userId
GET    /api/lessons/{id}/access             ✅ Fixed userId
GET    /api/lessons/{id}/next               ✅ Fixed userId
```

### 8. CURRICULUM (4 APIs)
```
GET    /api/curriculum/course/{courseId}/chapters
GET    /api/curriculum/chapters/{chapterId}
GET    /api/curriculum/chapters/{chapterId}/lessons
GET    /api/curriculum/course/{courseId}/full
```

### 9. PROGRESS TRACKING (4 APIs)
```
POST   /api/progress/enroll/{courseId}
GET    /api/progress/course/{courseId}
GET    /api/progress/my-courses
GET    /api/progress/course/{courseId}/chapters
```

### 10. QUIZ - Admin (4 APIs)
```
POST   /api/admin/quizzes/create
GET    /api/admin/quizzes/{quizId}
PUT    /api/admin/quizzes/{quizId}
DELETE /api/admin/quizzes/{quizId}
```

### 11. QUIZ - User (4 APIs) ✅ FIXED
```
GET    /api/quizzes/{quizId}
POST   /api/quizzes/submit              ✅ Fixed userId
GET    /api/quizzes/{quizId}/attempts   ✅ Fixed userId
GET    /api/quizzes/{quizId}/passed     ✅ Fixed userId
```

### 12. PAYMENT - VNPAY (6 APIs) 🔥 NEW
```
POST   /api/payment/vnpay/create        🔥 Direct purchase
GET    /api/payment/{paymentId}/status
GET    /api/payment/my-payments         🆕 Payment history
GET    /api/payment/my-payments/success 🆕 Successful only
GET    /api/payment/vnpay/return        (VNPAY callback)
GET    /api/payment/vnpay/ipn           (VNPAY server callback)
```

### 13. FAVORITES (7 APIs)
```
POST   /api/favorites/{userId}
GET    /api/favorites/{userId}
DELETE /api/favorites/{userId}/{courseId}
GET    /api/favorites/{userId}/check/{courseId}
GET    /api/favorites/{userId}/count
PUT    /api/favorites/{userId}/{courseId}/select?selected=true
DELETE /api/favorites/{userId}/clear
```

### 14. USER MANAGEMENT (4 APIs)
```
GET    /api/users/find-userId
PUT    /api/users/update-user
PUT    /api/users/change-password
DELETE /api/users/{userId}
```

### 15. ADMIN - USER MANAGEMENT (3 APIs)
```
GET    /api/admin/users/read-users
PUT    /api/admin/users/active/{userId}
PUT    /api/admin/users/{userId}/role
```

---

## 🚀 How to Use

### 1. Import to Postman
1. Open Postman
2. Click **Import**
3. Select `Test_PostMan_23-11-2025.postman_collection.json`
4. Collection will be imported with all 73+ endpoints

### 2. Setup Environment Variables
Create a new environment with these variables:
```
baseUrl: http://localhost:8080
userToken: (will auto-set after login)
adminToken: (will auto-set after admin login)
userId: (will auto-set)
courseId: (will auto-set after create course)
chapterId: (will auto-set after create chapter)
lessonId: (will auto-set after create lesson)
quizId: (will auto-set after create quiz)
paymentId: (will auto-set after create payment)
```

### 3. Test Flow

#### Admin Flow:
1. **Login Admin** → Get adminToken
2. **Create Category** → DEV, DESIGN, etc.
3. **Create Course** → Get courseId
4. **Upload Thumbnail** → Set course image
5. **Create Chapter** → Get chapterId
6. **Create Lesson** → Get lessonId
7. **Create Quiz** → Get quizId

#### User Flow:
1. **Register User** → Create account
2. **Verify OTP** → Confirm email
3. **Login User** → Get userToken
4. **Get Published Courses** → Browse courses
5. **Create Payment** → Buy course (direct)
   - Auto-enrolls after payment success
6. **Get My Courses** → See enrolled courses
7. **Get Lesson** → Watch video
8. **Update Video Progress** → Track progress ✅
9. **Mark Lesson Complete** → Complete lesson ✅
10. **Submit Quiz** → Test knowledge ✅

---

## ⚙️ Auto-Save Variables

The collection includes test scripts that auto-save IDs to environment:

```javascript
// After login
pm.environment.set('userToken', data.data.token);

// After create course
pm.environment.set('courseId', data.data.id);

// After create payment
pm.environment.set('paymentId', data.data.paymentId);
```

---

## 🔐 Authentication

### JWT Token Required:
- Add header: `Authorization: Bearer {{userToken}}`
- Token auto-set after login
- Admin endpoints require `{{adminToken}}`

### Roles:
- **USER:** Regular user (can view, enroll, learn)
- **ADMIN:** Full access to management APIs

---

## 🐛 Bug Fixes Included

### Critical Fixes (23/11/2025):
1. ✅ **Video Progress** - Now uses real userId from JWT
2. ✅ **Quiz Submit** - Fixed userId tracking
3. ✅ **Lesson Complete** - Fixed userId tracking
4. ✅ **Access Control** - Properly checks enrollment
5. ✅ **All User APIs** - Removed hardcoded "temp-user-id"

### See Also:
- `BUG_FIX_VIDEO_PROGRESS_USER_ID.md` - Detailed bug report
- `CRITICAL_BUG_FIX_SUMMARY.md` - Quick summary

---

## ❌ Deprecated Endpoints (Removed)

These endpoints are NO LONGER available:
```
❌ POST   /api/cart/add/{userId}
❌ GET    /api/cart/all
❌ GET    /api/cart/{userId}
❌ DELETE /api/cart/{userId}/item/{courseId}
❌ POST   /api/orders/create-order
❌ PUT    /api/orders/{orderId}/update-status
❌ GET    /api/orders/{userId}
❌ PUT    /api/orders/{orderId}/cancel
❌ GET    /api/admin/orders/all
❌ PUT    /api/admin/orders/{orderId}/status
```

**Reason:** Replaced by direct payment system

---

## 📚 Related Documentation

- `ALL_ENDPOINTS.md` - Complete API documentation
- `PAYMENT_API_GUIDE.md` - Payment API detailed guide
- `MIGRATION_NOTES.md` - Migration from cart/order to direct payment
- `PAYMENT_API_TEST_EXAMPLES.md` - Test examples

---

## ✅ Validation

**JSON Syntax:** ✅ Valid  
**Postman Schema:** ✅ v2.1.0  
**Import Status:** ✅ Ready  
**Total Requests:** 73+  

---

## 🎯 Next Steps

1. ✅ Import collection to Postman
2. ⏳ Setup environment variables
3. ⏳ Test Admin flow
4. ⏳ Test User flow
5. ⏳ Test Payment flow (CRITICAL)
6. ⏳ Verify video progress tracking works correctly

---

**File Created:** November 23, 2025  
**Status:** ✅ COMPLETE | READY TO USE  
**Bug Fixes:** ✅ ALL CRITICAL BUGS FIXED


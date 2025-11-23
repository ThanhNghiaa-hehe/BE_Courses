# Migration Notes - Loại bỏ Cart & Order Module

**Ngày thực hiện:** 23/11/2025  
**Version:** 2.0

## Tổng quan thay đổi

Hệ thống đã được cập nhật để **loại bỏ hoàn toàn module Cart và Order**, thay vào đó sử dụng **Payment trực tiếp** để mua khóa học.

---

## ✅ Đã thực hiện

### 1. **Cập nhật Payment Model**
- ✅ Thêm field `courses` (List<PaymentCourseItem>) để lưu thông tin khóa học trực tiếp
- ✅ Xóa field `cartId` (không còn cần cart)
- ✅ Thêm inner class `PaymentCourseItem` chứa đầy đủ thông tin khóa học:
  - courseId, title, thumbnailUrl
  - price, discountedPrice, discountPercent
  - instructorName, level

**File:** `src/main/java/com/example/cake/payment/model/Payment.java`

### 2. **Cập nhật PaymentService**
- ✅ Xóa dependency: `CartRepository`
- ✅ Thêm dependency: `CourseRepository`
- ✅ Method `createVNPayPayment()`:
  - Nhận `List<String> courseIds` thay vì lấy từ cart
  - Validate courses (tồn tại, published, chưa enroll)
  - Lưu thông tin courses vào payment
  - Tính tổng tiền trực tiếp từ courses
- ✅ Method `processVNPayReturn()`:
  - Enroll courses trực tiếp từ `payment.getCourses()`
  - Không còn xóa cart
- ✅ Thêm 2 methods mới:
  - `getUserPayments()` - Lấy tất cả payments của user
  - `getUserSuccessfulPayments()` - Lấy payments thành công

**File:** `src/main/java/com/example/cake/payment/service/PaymentService.java`

### 3. **Cập nhật PaymentController**
- ✅ Endpoint `POST /api/payment/vnpay/create`:
  - Request body thay đổi: nhận `courseIds` (List<String>)
  - Validate courseIds không rỗng
- ✅ Thêm 2 endpoints mới:
  - `GET /api/payment/my-payments` - Lịch sử thanh toán
  - `GET /api/payment/my-payments/success` - Thanh toán thành công

**File:** `src/main/java/com/example/cake/payment/controller/PaymentController.java`

### 4. **Vô hiệu hóa Cart & Order Controllers**
- ✅ **CartController**: Comment `@RestController` và `@RequestMapping`
- ✅ **OrderController**: Comment `@RestController` và `@RequestMapping`
- ✅ **OrderAdminController**: Comment `@RestController` và `@RequestMapping`
- ✅ Thêm comment DEPRECATED với lý do

**Files:**
- `src/main/java/com/example/cake/cart/controller/CartController.java`
- `src/main/java/com/example/cake/order/controller/OrderController.java`
- `src/main/java/com/example/cake/order/controller/OrderAdminController.java`

### 5. **Fix compilation error**
- ✅ Sửa missing import `lombok.Data` trong RegisterRequest.java

### 6. **Documentation**
- ✅ Tạo `PAYMENT_API_GUIDE.md` - Hướng dẫn API mới chi tiết
- ✅ Tạo `MIGRATION_NOTES.md` - File này

---

## 📋 API Endpoints

### ❌ Đã vô hiệu hóa (Deprecated)
```
❌ GET    /api/cart/all
❌ POST   /api/cart/add/{userId}
❌ GET    /api/cart/{userId}
❌ DELETE /api/cart/{userId}/item/{courseId}

❌ POST   /api/orders/create-order
❌ PUT    /api/orders/{orderId}/status
❌ GET    /api/orders/user/{userId}
❌ POST   /api/orders/{orderId}/cancel

❌ GET    /api/admin/orders/all
❌ PUT    /api/admin/orders/{orderId}/status
```

### ✅ API mới (Active)
```
✅ POST   /api/payment/vnpay/create          - Tạo thanh toán (với courseIds)
✅ GET    /api/payment/vnpay/return          - VNPAY return callback
✅ GET    /api/payment/vnpay/ipn             - VNPAY IPN callback
✅ GET    /api/payment/{paymentId}/status    - Lấy thông tin payment
✅ GET    /api/payment/my-payments           - Lịch sử thanh toán (NEW)
✅ GET    /api/payment/my-payments/success   - Thanh toán thành công (NEW)
```

---

## 🔄 Flow mới

### Trước đây:
```
Course → Add to Cart → View Cart → Create Order → Payment → Enroll
```

### Bây giờ:
```
Course → Select Courses → Payment → Enroll ✅
```

---

## 💻 Request/Response Examples

### Tạo thanh toán mới
```http
POST /api/payment/vnpay/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "courseIds": ["674115f9c62e0f3dd4a83e37", "674115f9c62e0f3dd4a83e38"],
  "orderInfo": "Thanh toan 2 khoa hoc"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "paymentId": "67419a3c8f2b1e4d5c6789ab"
  }
}
```

### Lấy lịch sử thanh toán
```http
GET /api/payment/my-payments
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy lịch sử thanh toán thành công",
  "data": [
    {
      "id": "67419a3c8f2b1e4d5c6789ab",
      "userId": "user123",
      "courses": [
        {
          "courseId": "674115f9c62e0f3dd4a83e37",
          "title": "React Hooks Cơ Bản",
          "price": 600000,
          "discountedPrice": 500000,
          "discountPercent": 17,
          "instructorName": "Nguyen Van A",
          "level": "BEGINNER"
        }
      ],
      "amount": 500000,
      "status": "SUCCESS",
      "paymentMethod": "VNPAY",
      "vnpResponseCode": "00",
      "createdAt": "2025-11-23T10:00:00",
      "paidAt": "2025-11-23T10:05:00"
    }
  ]
}
```

---

## 🗄️ Database Changes

### Payment Collection
**Trước:**
```javascript
{
  _id: ObjectId,
  userId: String,
  cartId: String,      // ❌ Removed
  amount: Number,
  status: String,
  // ...
}
```

**Sau:**
```javascript
{
  _id: ObjectId,
  userId: String,
  courses: [           // ✅ Added
    {
      courseId: String,
      title: String,
      thumbnailUrl: String,
      price: Number,
      discountedPrice: Number,
      discountPercent: Number,
      instructorName: String,
      level: String
    }
  ],
  amount: Number,
  status: String,
  // ...
}
```

**Lưu ý:** Data cũ vẫn tồn tại trong DB nhưng không được sử dụng. Không cần migration.

---

## ✅ Testing Checklist

### Manual Testing:
- [ ] Tạo payment với 1 khóa học
- [ ] Tạo payment với nhiều khóa học
- [ ] Thanh toán thành công trên VNPAY sandbox
- [ ] Kiểm tra course được enroll sau thanh toán
- [ ] Lấy lịch sử thanh toán
- [ ] Test với khóa học đã enroll (should fail)
- [ ] Test với khóa học chưa publish (should fail)
- [ ] Test với courseIds rỗng (should fail)

### API Testing với Postman:
- [ ] Import collection mới
- [ ] Test tất cả endpoints payment
- [ ] Verify response structure
- [ ] Test authentication

---

## 🚀 Deployment Notes

### Build & Deploy:
```bash
# Clean và compile
mvn clean compile

# Run tests
mvn test

# Package
mvn clean package

# Run application
java -jar target/clickUp-0.0.1-SNAPSHOT.jar
```

### Environment Variables:
Không có thay đổi về environment variables.

### Configuration:
Không cần thay đổi `application.yml`.

---

## 📝 Frontend Changes Required

### 1. Remove Cart/Order code
```javascript
// ❌ Remove these
import CartService from './services/CartService';
import OrderService from './services/OrderService';

// Xóa tất cả calls đến:
// - /api/cart/*
// - /api/orders/*
```

### 2. Implement Direct Payment
```javascript
// ✅ Add new
import PaymentService from './services/PaymentService';

// Buy course directly
const handleBuyCourse = async (courseIds) => {
  const response = await PaymentService.createPayment({
    courseIds: courseIds,
    orderInfo: 'Thanh toan khoa hoc'
  });
  
  if (response.success) {
    // Redirect to VNPAY
    window.location.href = response.data.paymentUrl;
  }
};
```

### 3. Handle VNPAY Return
```javascript
// Parse return URL params
useEffect(() => {
  const params = new URLSearchParams(window.location.search);
  const vnpResponseCode = params.get('vnp_ResponseCode');
  
  if (vnpResponseCode === '00') {
    showSuccessMessage('Thanh toán thành công!');
    navigate('/my-courses');
  } else {
    showErrorMessage('Thanh toán thất bại');
  }
}, []);
```

### 4. Payment History Page
```javascript
// New page: PaymentHistory.jsx
const PaymentHistory = () => {
  const [payments, setPayments] = useState([]);
  
  useEffect(() => {
    PaymentService.getMyPayments().then(res => {
      setPayments(res.data);
    });
  }, []);
  
  return (
    <div>
      {payments.map(payment => (
        <PaymentCard key={payment.id} payment={payment} />
      ))}
    </div>
  );
};
```

---

## 🔐 Security Notes

- ✅ Tất cả payment endpoints yêu cầu authentication
- ✅ Validate user ownership (userId từ JWT)
- ✅ Validate course existence và published status
- ✅ Prevent duplicate enrollment
- ✅ VNPAY signature verification

---

## 🐛 Known Issues & Solutions

### Issue 1: Old cart/order data
**Solution:** Data cũ không ảnh hưởng. Có thể giữ lại để tham khảo hoặc xóa thủ công.

### Issue 2: Frontend có thể còn gọi cart/order APIs
**Solution:** Các controllers đã bị vô hiệu hóa, sẽ return 404. Update frontend ASAP.

---

## 📚 Related Documentation

- `PAYMENT_API_GUIDE.md` - API documentation chi tiết
- `Complete_API_Collection.postman_collection.json` - Postman collection (cần update)
- `VIDEO_PROGRESS_FRONTEND_GUIDE.md` - Frontend guides

---

## 👥 Contact & Support

Nếu có vấn đề hoặc câu hỏi về migration này, vui lòng liên hệ team development.

---

**Status:** ✅ Completed  
**Tested:** ✅ Compilation successful  
**Next Steps:** Test APIs với Postman, update frontend


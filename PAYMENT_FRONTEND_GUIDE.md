# 💳 HƯỚNG DẪN XỬ LÝ THANH TOÁN Ở FRONTEND - REACT

## 📌 TỔNG QUAN

Backend API **trả về JSON** cho Frontend React xử lý và hiển thị giao diện.

### Luồng hoạt động:
```
User → Click "Thanh toán" 
     → Frontend gọi API create payment
     → Nhận paymentUrl từ Backend
     → Redirect sang VNPAY
     → User thanh toán trên VNPAY
     → VNPAY redirect về Backend: /api/payment/vnpay/return
     → Backend xử lý payment → Trả về JSON
     → Frontend nhận JSON response
     → Frontend hiển thị kết quả (success/failed)
```

### 🎯 API Endpoint:
```
GET /api/payment/vnpay/return?vnp_params...
Response: JSON
```

---

## 🎯 BƯỚC 1: TẠO TRANG THANH TOÁN

### **File: `pages/payment/checkout.jsx` (hoặc tương đương)**

```javascript
import { useState } from 'react';
import axios from 'axios';

function CheckoutPage() {
  const [loading, setLoading] = useState(false);
  const [selectedCourses, setSelectedCourses] = useState([]);

  const handlePayment = async () => {
    try {
      setLoading(true);
      
      // Gọi API tạo payment
      const response = await axios.post(
        'http://localhost:8080/api/payment/vnpay/create',
        {
          courseIds: selectedCourses.map(c => c.id),
          orderInfo: 'Thanh toan khoa hoc'
        },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );

      if (response.data.success) {
        // Lấy payment URL từ response
        const paymentUrl = response.data.data.paymentUrl;
        
        console.log('✅ Payment URL:', paymentUrl);
        
        // Redirect sang VNPAY
        window.location.href = paymentUrl;
      } else {
        alert('Lỗi: ' + response.data.message);
      }
    } catch (error) {
      console.error('Payment error:', error);
      alert('Không thể tạo thanh toán');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-page">
      <h1>Thanh toán</h1>
      
      {/* Danh sách khóa học đã chọn */}
      <div className="course-list">
        {selectedCourses.map(course => (
          <div key={course.id} className="course-item">
            <h3>{course.title}</h3>
            <p>{course.price.toLocaleString('vi-VN')} VNĐ</p>
          </div>
        ))}
      </div>

      {/* Tổng tiền */}
      <div className="total">
        <h2>Tổng cộng: {
          selectedCourses.reduce((sum, c) => sum + c.price, 0).toLocaleString('vi-VN')
        } VNĐ</h2>
      </div>

      {/* Nút thanh toán */}
      <button 
        onClick={handlePayment} 
        disabled={loading || selectedCourses.length === 0}
        className="btn-payment"
      >
        {loading ? 'Đang xử lý...' : 'Thanh toán qua VNPAY'}
      </button>
    </div>
  );
}

export default CheckoutPage;
```

---

## 🎯 BƯỚC 2: TẠO TRANG KẾT QUẢ THANH TOÁN

### **File: `pages/payment/result.jsx`**

```javascript
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const [status, setStatus] = useState('loading');
  const [paymentData, setPaymentData] = useState(null);

  useEffect(() => {
    // Lấy params từ URL
    const status = searchParams.get('status');
    const paymentId = searchParams.get('paymentId');
    const amount = searchParams.get('amount');
    const courses = searchParams.get('courses');
    const message = searchParams.get('message');
    const code = searchParams.get('code');

    console.log('Payment Result:', { status, paymentId, amount, courses, message });

    if (status === 'success') {
      setStatus('success');
      setPaymentData({
        paymentId,
        amount: parseInt(amount),
        coursesEnrolled: parseInt(courses),
        message: decodeURIComponent(message || '')
      });

      // Optional: Gọi API để lấy chi tiết payment
      fetchPaymentDetails(paymentId);

    } else if (status === 'failed') {
      setStatus('failed');
      setPaymentData({
        message: decodeURIComponent(message || ''),
        code
      });

    } else if (status === 'error') {
      setStatus('error');
      setPaymentData({
        message: decodeURIComponent(message || 'Có lỗi xảy ra')
      });
    }
  }, [searchParams]);

  const fetchPaymentDetails = async (paymentId) => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/payment/${paymentId}/status`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );

      if (response.data.success) {
        console.log('Payment details:', response.data.data);
        // Có thể update thêm thông tin chi tiết
      }
    } catch (error) {
      console.error('Error fetching payment details:', error);
    }
  };

  const handleGoToCourses = () => {
    navigate('/my-courses');
  };

  const handleGoHome = () => {
    navigate('/');
  };

  // Loading state
  if (status === 'loading') {
    return (
      <div className="payment-result loading">
        <div className="spinner"></div>
        <p>Đang xử lý kết quả thanh toán...</p>
      </div>
    );
  }

  // Success state
  if (status === 'success') {
    return (
      <div className="payment-result success">
        <div className="success-icon">✅</div>
        <h1>Thanh toán thành công!</h1>
        
        <div className="payment-info">
          <p className="message">{paymentData.message}</p>
          
          <div className="details">
            <div className="detail-item">
              <span className="label">Mã giao dịch:</span>
              <span className="value">{paymentData.paymentId}</span>
            </div>
            
            <div className="detail-item">
              <span className="label">Số tiền:</span>
              <span className="value">
                {paymentData.amount.toLocaleString('vi-VN')} VNĐ
              </span>
            </div>
            
            <div className="detail-item">
              <span className="label">Số khóa học:</span>
              <span className="value">{paymentData.coursesEnrolled}</span>
            </div>
          </div>
        </div>

        <div className="actions">
          <button onClick={handleGoToCourses} className="btn-primary">
            Vào học ngay
          </button>
          <button onClick={handleGoHome} className="btn-secondary">
            Về trang chủ
          </button>
        </div>
      </div>
    );
  }

  // Failed state
  if (status === 'failed') {
    return (
      <div className="payment-result failed">
        <div className="failed-icon">❌</div>
        <h1>Thanh toán thất bại</h1>
        
        <div className="payment-info">
          <p className="message">{paymentData.message}</p>
          {paymentData.code && (
            <p className="error-code">Mã lỗi: {paymentData.code}</p>
          )}
        </div>

        <div className="actions">
          <button onClick={() => navigate(-1)} className="btn-primary">
            Thử lại
          </button>
          <button onClick={handleGoHome} className="btn-secondary">
            Về trang chủ
          </button>
        </div>
      </div>
    );
  }

  // Error state
  return (
    <div className="payment-result error">
      <div className="error-icon">⚠️</div>
      <h1>Có lỗi xảy ra</h1>
      
      <div className="payment-info">
        <p className="message">{paymentData?.message || 'Không thể xử lý thanh toán'}</p>
      </div>

      <div className="actions">
        <button onClick={handleGoHome} className="btn-primary">
          Về trang chủ
        </button>
      </div>
    </div>
  );
}

export default PaymentResultPage;
```

---

## 🎨 BƯỚC 3: STYLING (CSS)

### **File: `styles/payment-result.css`**

```css
.payment-result {
  max-width: 600px;
  margin: 50px auto;
  padding: 40px;
  text-align: center;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

/* Loading */
.payment-result.loading .spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Success */
.payment-result.success .success-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.payment-result.success h1 {
  color: #27ae60;
  margin-bottom: 30px;
}

/* Failed */
.payment-result.failed .failed-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.payment-result.failed h1 {
  color: #e74c3c;
  margin-bottom: 30px;
}

/* Error */
.payment-result.error .error-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.payment-result.error h1 {
  color: #f39c12;
  margin-bottom: 30px;
}

/* Payment Info */
.payment-info {
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.payment-info .message {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.details {
  text-align: left;
  margin: 20px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  font-weight: 500;
  color: #666;
}

.detail-item .value {
  font-weight: 600;
  color: #333;
}

.error-code {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
}

/* Actions */
.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn-primary,
.btn-secondary {
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.3);
}

.btn-secondary {
  background: #ecf0f1;
  color: #333;
}

.btn-secondary:hover {
  background: #bdc3c7;
}

/* Responsive */
@media (max-width: 768px) {
  .payment-result {
    margin: 20px;
    padding: 20px;
  }

  .actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
```

---

## 🔧 BƯỚC 4: CẤU HÌNH ROUTING

### **File: `App.jsx` hoặc `routes.jsx`**

```javascript
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import CheckoutPage from './pages/payment/checkout';
import PaymentResultPage from './pages/payment/result';
import MyCoursesPage from './pages/my-courses';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ... other routes ... */}
        
        <Route path="/payment/checkout" element={<CheckoutPage />} />
        <Route path="/payment/result" element={<PaymentResultPage />} />
        <Route path="/my-courses" element={<MyCoursesPage />} />
        
        {/* ... other routes ... */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## 🎯 BƯỚC 5: XỬ LÝ SAU KHI THANH TOÁN THÀNH CÔNG

### **Optional: Tự động cập nhật danh sách khóa học**

```javascript
// Trong PaymentResultPage, sau khi thanh toán thành công
useEffect(() => {
  if (status === 'success') {
    // Xóa cache khóa học (nếu có)
    localStorage.removeItem('cached_courses');
    
    // Cập nhật lại danh sách khóa học đã mua
    fetchMyCourses();
    
    // Hiển thị notification
    showNotification('Bạn đã đăng ký khóa học thành công!');
  }
}, [status]);

const fetchMyCourses = async () => {
  try {
    const response = await axios.get(
      'http://localhost:8080/api/progress/my-courses',
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    
    // Lưu vào state hoặc context
    console.log('My courses:', response.data.data);
  } catch (error) {
    console.error('Error fetching courses:', error);
  }
};
```

---

## 📝 API RESPONSE FORMAT

### **Backend trả về JSON:**

**Success:**
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "status": "success",
    "message": "Thanh toán thành công",
    "paymentId": "PAY_1234567890",
    "amount": 500000,
    "coursesEnrolled": 2
  }
}
```

**Failed:**
```json
{
  "success": false,
  "message": "Giao dịch bị hủy",
  "data": {
    "status": "failed",
    "message": "Giao dịch bị hủy",
    "responseCode": "24"
  }
}
```

---

## 🧪 TESTING

### **Test Case 1: Thanh toán thành công**

1. Chọn khóa học → Click "Thanh toán"
2. Được redirect sang sandbox VNPAY
3. Nhập thông tin test:
   - **Số thẻ:** 9704198526191432198
   - **Tên:** NGUYEN VAN A
   - **Ngày hết hạn:** 07/15
   - **Mã OTP:** 123456
4. Click "Thanh toán"
5. VNPAY redirect về: `/api/payment/vnpay/return?vnp_...`
6. Backend trả về JSON với `success: true`
7. Frontend nhận response và hiển thị trang success
8. Click "Vào học ngay" → Chuyển sang `/my-courses`

### **Test Case 2: Thanh toán thất bại**

1. Làm tương tự nhưng click "Hủy" trên VNPAY
2. VNPAY redirect về: `/api/payment/vnpay/return?vnp_...`
3. Backend trả về JSON với `success: false`
4. Frontend nhận response và hiển thị trang failed
5. Click "Thử lại" → Quay lại trang checkout

---

## 🔍 DEBUG

### **Check API Response:**

```javascript
// Trong PaymentResultPage - khi component mount
useEffect(() => {
  console.log('Current URL:', window.location.href);
  // URL sẽ là: http://localhost:8080/api/payment/vnpay/return?vnp_...
  
  // Browser sẽ tự động gọi API này và nhận JSON response
}, []);
```

### **Check Backend logs:**

```bash
# Terminal chạy Spring Boot
VNPAY return callback received with 15 params
Params: {vnp_Amount=50000000, vnp_BankCode=NCB, ...}
✅ Payment SUCCESS: PAY_1234567890
```

### **Network tab (Browser DevTools):**

1. Mở DevTools → Network
2. Thanh toán trên VNPAY
3. Xem request:
   - URL: `http://localhost:8080/api/payment/vnpay/return?vnp_...`
   - Method: GET
   - Response: JSON với `{success: true, data: {...}}`

---

## ⚡ TROUBLESHOOTING

### **Vấn đề 1: CORS Error**

Nếu gặp lỗi CORS khi redirect, thêm config:

```java
// CorsConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("*")
            .allowCredentials(true)
            .allowedHeaders("*");
}
```

### **Vấn đề 2: Frontend không nhận được params**

Check:
- URL có đúng format không?
- `useSearchParams()` đã import đúng chưa?
- React Router version >= 6

### **Vấn đề 3: Redirect loop**

- Đảm bảo Backend chỉ redirect 1 lần
- Frontend không tự động redirect lại

---

## 📦 PACKAGES CẦN THIẾT

```bash
# React Router (nếu chưa có)
npm install react-router-dom

# Axios (nếu chưa có)
npm install axios

# Optional: Notification library
npm install react-toastify
```

---

## ✅ CHECKLIST

- [ ] Backend đã sửa endpoint `/vnpay/return` để redirect
- [ ] Frontend đã tạo route `/payment/result`
- [ ] Frontend xử lý được URL params (status, paymentId, amount...)
- [ ] CSS đã được style đẹp
- [ ] Test thanh toán thành công
- [ ] Test thanh toán thất bại
- [ ] Xử lý loading state
- [ ] Xử lý error state
- [ ] Redirect về My Courses sau khi thành công

---

## 🎯 KẾT QUẢ MONG ĐỢI

**API Response khi thành công:**
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "status": "success",
    "paymentId": "PAY_1234567890",
    "amount": 500000,
    "coursesEnrolled": 2
  }
}
```

**API Response khi thất bại:**
```json
{
  "success": false,
  "message": "Giao dịch bị hủy",
  "data": {
    "status": "failed",
    "responseCode": "24"
  }
}
```

**Giao diện React hiển thị:**
- ✅ Icon lớn (✅ hoặc ❌)
- ✅ Thông báo rõ ràng
- ✅ Thông tin chi tiết (paymentId, amount, số khóa học)
- ✅ Nút action (Vào học ngay / Thử lại / Về trang chủ)

---

**🎉 Hoàn tất! Backend API `/api/payment/vnpay/return` sẵn sàng trả JSON cho React Frontend xử lý!**


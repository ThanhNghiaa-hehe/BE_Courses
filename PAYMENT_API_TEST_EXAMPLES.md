# Payment API Test Examples

## 1. Create Payment (Direct Course Purchase)

### Request:
```http
POST http://localhost:8080/api/payment/vnpay/create
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.xxxxx
Content-Type: application/json

{
  "courseIds": ["674115f9c62e0f3dd4a83e37"],
  "orderInfo": "Thanh toan khoa hoc React Hooks"
}
```

### Response (Success):
```json
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=50000000&vnp_Command=pay&vnp_CreateDate=20251123104500&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+khoa+hoc+React+Hooks&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayment%2Fvnpay%2Freturn&vnp_TmnCode=YOUR_TMN_CODE&vnp_TxnRef=67419a3c8f2b1e4d5c6789ab&vnp_Version=2.1.0&vnp_SecureHash=xxxxx",
    "paymentId": "67419a3c8f2b1e4d5c6789ab"
  }
}
```

### Response (Error - Course not found):
```json
{
  "success": false,
  "message": "Không tìm thấy khóa học: 674115f9c62e0f3dd4a83e37",
  "data": null
}
```

### Response (Error - Already enrolled):
```json
{
  "success": false,
  "message": "Bạn đã đăng ký khóa học: React Hooks Cơ Bản",
  "data": null
}
```

---

## 2. Buy Multiple Courses

### Request:
```http
POST http://localhost:8080/api/payment/vnpay/create
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "courseIds": [
    "674115f9c62e0f3dd4a83e37",
    "674115f9c62e0f3dd4a83e38",
    "674115f9c62e0f3dd4a83e39"
  ],
  "orderInfo": "Thanh toan 3 khoa hoc: React, Node.js, MongoDB"
}
```

### Response:
```json
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "paymentId": "67419a3c8f2b1e4d5c6789ac"
  }
}
```

---

## 3. VNPAY Return (Success)

Sau khi thanh toán thành công, VNPAY redirect về:

```
GET http://localhost:8080/api/payment/vnpay/return?vnp_Amount=50000000&vnp_BankCode=NCB&vnp_BankTranNo=VNP01234567&vnp_CardType=ATM&vnp_OrderInfo=Thanh+toan+khoa+hoc&vnp_PayDate=20251123104530&vnp_ResponseCode=00&vnp_TmnCode=YOUR_TMN_CODE&vnp_TransactionNo=14123456&vnp_TransactionStatus=00&vnp_TxnRef=67419a3c8f2b1e4d5c6789ab&vnp_SecureHash=xxxxx
```

### Response:
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "status": "success",
    "message": "Thanh toán thành công",
    "paymentId": "67419a3c8f2b1e4d5c6789ab",
    "amount": 500000,
    "coursesEnrolled": 1
  }
}
```

---

## 4. VNPAY Return (Failed/Cancelled)

```
GET http://localhost:8080/api/payment/vnpay/return?vnp_Amount=50000000&vnp_OrderInfo=Thanh+toan+khoa+hoc&vnp_ResponseCode=24&vnp_TxnRef=67419a3c8f2b1e4d5c6789ab&vnp_SecureHash=xxxxx
```

### Response:
```json
{
  "success": false,
  "message": "Giao dịch không thành công do: Khách hàng hủy giao dịch",
  "data": {
    "status": "failed",
    "message": "Giao dịch không thành công do: Khách hàng hủy giao dịch",
    "responseCode": "24"
  }
}
```

---

## 5. Get Payment Status

### Request:
```http
GET http://localhost:8080/api/payment/67419a3c8f2b1e4d5c6789ab/status
Authorization: Bearer {your_jwt_token}
```

### Response:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "67419a3c8f2b1e4d5c6789ab",
    "userId": "user123",
    "courses": [
      {
        "courseId": "674115f9c62e0f3dd4a83e37",
        "title": "React Hooks Cơ Bản",
        "thumbnailUrl": "https://example.com/react-hooks.jpg",
        "price": 600000,
        "discountedPrice": 500000,
        "discountPercent": 17,
        "instructorName": "Nguyen Van A",
        "level": "BEGINNER"
      }
    ],
    "amount": 500000,
    "vnpTxnRef": "67419a3c8f2b1e4d5c6789ab",
    "vnpTransactionNo": "14123456",
    "vnpBankCode": "NCB",
    "vnpResponseCode": "00",
    "vnpOrderInfo": "Thanh toan khoa hoc React Hooks",
    "status": "SUCCESS",
    "paymentMethod": "VNPAY",
    "ipAddress": "127.0.0.1",
    "locale": "vn",
    "createdAt": "2025-11-23T10:45:00",
    "paidAt": "2025-11-23T10:45:30"
  }
}
```

---

## 6. Get My Payment History

### Request:
```http
GET http://localhost:8080/api/payment/my-payments
Authorization: Bearer {your_jwt_token}
```

### Response:
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
      "createdAt": "2025-11-23T10:45:00",
      "paidAt": "2025-11-23T10:45:30"
    },
    {
      "id": "67419a3c8f2b1e4d5c6789aa",
      "userId": "user123",
      "courses": [
        {
          "courseId": "674115f9c62e0f3dd4a83e38",
          "title": "Node.js Advanced",
          "price": 800000,
          "discountedPrice": 700000,
          "discountPercent": 12,
          "instructorName": "Tran Van B",
          "level": "ADVANCED"
        }
      ],
      "amount": 700000,
      "status": "PENDING",
      "paymentMethod": "VNPAY",
      "createdAt": "2025-11-22T15:30:00",
      "paidAt": null
    }
  ]
}
```

---

## 7. Get My Successful Payments

### Request:
```http
GET http://localhost:8080/api/payment/my-payments/success
Authorization: Bearer {your_jwt_token}
```

### Response:
```json
{
  "success": true,
  "message": "Lấy lịch sử thanh toán thành công",
  "data": [
    {
      "id": "67419a3c8f2b1e4d5c6789ab",
      "userId": "user123",
      "courses": [...],
      "amount": 500000,
      "status": "SUCCESS",
      "createdAt": "2025-11-23T10:45:00",
      "paidAt": "2025-11-23T10:45:30"
    }
  ]
}
```

---

## Error Responses

### 401 Unauthorized (No Token)
```json
{
  "success": false,
  "message": "Vui lòng đăng nhập",
  "data": null
}
```

### 400 Bad Request (Empty courseIds)
```json
{
  "success": false,
  "message": "Vui lòng chọn ít nhất một khóa học",
  "data": null
}
```

### 400 Bad Request (Course not published)
```json
{
  "success": false,
  "message": "Khóa học chưa được công khai: React Advanced",
  "data": null
}
```

### 400 Bad Request (Already enrolled)
```json
{
  "success": false,
  "message": "Bạn đã đăng ký khóa học: React Hooks Cơ Bản",
  "data": null
}
```

---

## VNPAY Sandbox Test Data

### Test Card Info:
- **Ngân hàng:** NCB
- **Số thẻ:** 9704198526191432198
- **Tên chủ thẻ:** NGUYEN VAN A
- **Ngày phát hành:** 07/15
- **Mật khẩu OTP:** 123456

### VNPAY Response Codes:
- `00` - Giao dịch thành công
- `07` - Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)
- `09` - Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking
- `10` - Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần
- `11` - Giao dịch không thành công do: Đã hết hạn chờ thanh toán
- `12` - Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa
- `13` - Giao dịch không thành công do: Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)
- `24` - Giao dịch không thành công do: Khách hàng hủy giao dịch
- `51` - Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư
- `65` - Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá giới hạn giao dịch trong ngày
- `75` - Ngân hàng thanh toán đang bảo trì
- `79` - Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định
- `99` - Các lỗi khác

---

## Postman Collection

Import file `Complete_API_Collection.postman_collection.json` và update với các endpoints mới:

1. Create Payment
2. Get Payment Status
3. Get My Payments
4. Get My Successful Payments

---

## Notes

- Tất cả endpoints yêu cầu JWT token trong header `Authorization: Bearer {token}`
- VNPAY return URL được handle tự động, không cần gọi manually
- IPN URL chỉ được gọi bởi VNPAY server để confirm payment
- Payment history được sắp xếp theo `createdAt` DESC (mới nhất trước)


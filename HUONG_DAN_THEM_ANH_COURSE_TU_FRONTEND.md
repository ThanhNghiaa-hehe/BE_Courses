# HƯỚNG DẪN THÊM ẢNH THUMBNAIL CHO KHÓA HỌC TỪ FRONTEND

## 📌 Tổng Quan

Hiện tại Backend **CHƯA** có API upload ảnh cho khóa học. Bạn có 2 cách để thêm ảnh thumbnail:

### **Cách 1: Sử dụng ảnh đã có sẵn trong thư mục `uploads/courses/`** (Đơn giản nhất - Khuyến nghị)
### **Cách 2: Upload ảnh mới vào thư mục `uploads/courses/` rồi dùng URL**

---

## 🎯 CÁCH 1: SỬ DỤNG ẢNH CÓ SẴN (VÍ DỤ: html-css.jpg)

### Bước 1: Kiểm tra ảnh có tồn tại không

Mở trình duyệt và truy cập:
```
http://localhost:8080/static/courses/html-css.jpg
```

✅ Nếu thấy ảnh hiển thị → OK, ảnh đã sẵn sàng  
❌ Nếu bị 404 → Kiểm tra lại server có chạy không hoặc file có đúng tên không

---

### Bước 2: Tạo Khóa Học với Thumbnail URL

#### **API Endpoint:**
```
POST http://localhost:8080/api/admin/courses/create
```

#### **Headers:**
```
Authorization: Bearer <YOUR_ADMIN_TOKEN>
Content-Type: application/json
```

#### **Request Body:**
```json
{
  "categoryCode": "WEB",
  "title": "Khóa học HTML & CSS từ cơ bản đến nâng cao",
  "description": "Học HTML CSS từ đầu, xây dựng website responsive đẹp mắt",
  "price": 499000.0,
  "thumbnailUrl": "http://localhost:8080/static/courses/html-css.jpg",
  "duration": 30,
  "level": "Beginner",
  "isPublished": true,
  "instructorName": "Nguyễn Văn B",
  "rating": 4.5,
  "totalStudents": 850,
  "discountPercent": 15,
  "discountedPrice": 424150.0
}
```

#### **Response mong đợi:**
```json
{
  "success": true,
  "message": "Course created",
  "data": {
    "id": "691c79f6190d8c0f5aac76a0",
    "categoryCode": "WEB",
    "title": "Khóa học HTML & CSS từ cơ bản đến nâng cao",
    "description": "Học HTML CSS từ đầu, xây dựng website responsive đẹp mắt",
    "price": 499000.0,
    "thumbnailUrl": "http://localhost:8080/static/courses/html-css.jpg",
    "duration": 30,
    "level": "Beginner",
    "isPublished": true,
    "instructorName": "Nguyễn Văn B",
    "rating": 4.5,
    "totalStudents": 850,
    "discountPercent": 15,
    "discountedPrice": 424150.0
  }
}
```

---

## 🎯 CÁCH 2: THÊM ẢNH MỚI VÀO THỦ MỤC UPLOADS

### Bước 1: Copy ảnh vào thư mục

**Trên Windows:**
1. Mở File Explorer
2. Điều hướng đến: `D:\LapTrinhWebNangCao\nghia\uploads\courses\`
3. Copy/paste file ảnh của bạn vào đây (ví dụ: `java-spring-boot.jpg`)

**Lưu ý:**
- Đặt tên file không dấu, không khoảng trắng (vd: `java-spring-boot.jpg` ✅, `Khóa Học Java.jpg` ❌)
- Định dạng hỗ trợ: `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif`
- Kích thước khuyến nghị: < 2MB

### Bước 2: Kiểm tra ảnh đã access được chưa

Mở trình duyệt:
```
http://localhost:8080/static/courses/java-spring-boot.jpg
```

### Bước 3: Sử dụng URL trong Frontend

```json
{
  "categoryCode": "PROGRAM",
  "title": "Khóa học Java Spring Boot",
  "description": "Học Spring Boot từ A-Z",
  "price": 1800000.0,
  "thumbnailUrl": "http://localhost:8080/static/courses/java-spring-boot.jpg",
  "duration": 50,
  "level": "Advanced",
  "isPublished": true
}
```

---

## 💻 CODE MẪU CHO FRONTEND (React/Vue/Angular)

### **React Example:**

```jsx
import { useState } from 'react';
import axios from 'axios';

function CreateCourseForm() {
  const [formData, setFormData] = useState({
    categoryCode: 'WEB',
    title: '',
    description: '',
    price: 0,
    thumbnailUrl: 'http://localhost:8080/static/courses/html-css.jpg', // ← ẢNH CÓ SẴN
    duration: 0,
    level: 'Beginner',
    isPublished: false
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await axios.post(
        'http://localhost:8080/api/admin/courses/create',
        formData,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('adminToken')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log('Course created:', response.data);
      alert('Tạo khóa học thành công!');
    } catch (error) {
      console.error('Error:', error);
      alert('Lỗi: ' + error.response?.data?.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
      <input 
        type="text"
        placeholder="Tiêu đề khóa học"
        value={formData.title}
        onChange={(e) => setFormData({...formData, title: e.target.value})}
      />
      
      <input 
        type="text"
        placeholder="URL ảnh thumbnail"
        value={formData.thumbnailUrl}
        onChange={(e) => setFormData({...formData, thumbnailUrl: e.target.value})}
      />
      
      {/* Preview ảnh */}
      {formData.thumbnailUrl && (
        <img 
          src={formData.thumbnailUrl} 
          alt="Preview" 
          style={{width: '200px', height: 'auto'}}
        />
      )}
      
      <button type="submit">Tạo Khóa Học</button>
    </form>
  );
}
```

---

## 📝 DANH SÁCH CÁC ẢNH CÓ SẴN

Hiện tại trong thư mục `uploads/courses/`:
- ✅ `html-css.jpg` → URL: `http://localhost:8080/static/courses/html-css.jpg`

**Các ảnh khác bạn có thể thêm vào:**
- `python-basic.jpg`
- `javascript-advanced.jpg`
- `react-fullstack.jpg`
- `nodejs-api.jpg`
- ...

---

## 🔧 XỬ LÝ LỖI THƯỜNG GẶP

### ❌ Lỗi 404 khi truy cập ảnh

**Nguyên nhân:**
- File không tồn tại trong `uploads/courses/`
- Sai tên file (phân biệt hoa/thường)
- Server chưa chạy

**Giải pháp:**
```bash
# Kiểm tra file có tồn tại
dir D:\LapTrinhWebNangCao\nghia\uploads\courses

# Restart server
mvn spring-boot:run
```

---

### ❌ Ảnh không hiển thị trên Frontend

**Nguyên nhân:**
- URL không đúng format
- CORS issue
- Đường dẫn tương đối thay vì tuyệt đối

**Giải pháp:**
- Luôn dùng URL đầy đủ: `http://localhost:8080/static/courses/...`
- Kiểm tra Network tab trong DevTools
- Đảm bảo server đang chạy

---

### ❌ Lỗi 403 Forbidden khi tạo course

**Nguyên nhân:**
- Chưa đăng nhập với tài khoản ADMIN
- Token hết hạn
- Thiếu header Authorization

**Giải pháp:**
```javascript
// Đảm bảo có token ADMIN
const token = localStorage.getItem('adminToken');

axios.post(url, data, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 🎨 MẪU URL CHO CÁC LOẠI KHÓA HỌC

```javascript
const courseThumbnails = {
  // Lập trình Web
  html_css: 'http://localhost:8080/static/courses/html-css.jpg',
  javascript: 'http://localhost:8080/static/courses/javascript.jpg',
  react: 'http://localhost:8080/static/courses/react.jpg',
  
  // Backend
  java: 'http://localhost:8080/static/courses/java-spring-boot.jpg',
  nodejs: 'http://localhost:8080/static/courses/nodejs.jpg',
  python: 'http://localhost:8080/static/courses/python.jpg',
  
  // Mobile
  flutter: 'http://localhost:8080/static/courses/flutter.jpg',
  react_native: 'http://localhost:8080/static/courses/react-native.jpg',
  
  // Database
  mongodb: 'http://localhost:8080/static/courses/mongodb.jpg',
  mysql: 'http://localhost:8080/static/courses/mysql.jpg'
};
```

---

## 🚀 POSTMAN TEST NHANH

1. Mở Postman
2. Tạo request mới:
   - Method: `POST`
   - URL: `http://localhost:8080/api/admin/courses/create`
3. Headers:
   ```
   Authorization: Bearer eyJhbGc...YOUR_TOKEN
   Content-Type: application/json
   ```
4. Body (raw JSON):
   ```json
   {
     "categoryCode": "WEB",
     "title": "HTML CSS Cơ Bản",
     "description": "Khóa học HTML CSS cho người mới bắt đầu",
     "price": 499000,
     "thumbnailUrl": "http://localhost:8080/static/courses/html-css.jpg",
     "duration": 30,
     "level": "Beginner",
     "isPublished": true
   }
   ```
5. Click **Send**

---

## 📌 LƯU Ý QUAN TRỌNG

### ✅ Nên làm:
- Đặt tên file ảnh rõ ràng, không dấu
- Dùng URL tuyệt đối (có http://localhost:8080)
- Kiểm tra ảnh trên trình duyệt trước khi dùng
- Backup ảnh ở nơi khác phòng mất dữ liệu

### ❌ Không nên:
- Dùng đường dẫn tương đối (vd: `../uploads/courses/...`)
- Upload ảnh quá lớn (> 5MB)
- Đặt tên file có ký tự đặc biệt, khoảng trắng
- Hard-code `localhost` trong production (dùng biến môi trường)

---

## 🔮 TƯƠNG LAI: API UPLOAD ẢNH

Hiện tại backend chưa có endpoint upload ảnh. Nếu cần, sẽ tạo:

```
POST /api/admin/courses/uploadThumbnail
Content-Type: multipart/form-data

Form Data:
- file: [File binary]

Response:
{
  "success": true,
  "message": "Upload thành công",
  "data": {
    "thumbnailUrl": "http://localhost:8080/static/courses/abc-xyz.jpg"
  }
}
```

**→ Hiện tại chưa có, nên dùng Cách 1 hoặc Cách 2 ở trên.**

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. ✅ Server đang chạy (`http://localhost:8080`)
2. ✅ File ảnh tồn tại trong `uploads/courses/`
3. ✅ URL đầy đủ và đúng định dạng
4. ✅ Có token ADMIN hợp lệ
5. ✅ Truy cập trực tiếp URL ảnh trên trình duyệt

---

**Chúc bạn thành công! 🎉**


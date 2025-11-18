package com.example.cake.cart.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String courseId;           // ID khóa học (lấy từ course.id)
    private String title;              // Tên khóa học (lấy từ course.title)
    private String thumbnailUrl;       // Ảnh thumbnail khóa học (lấy từ course.thumbnailUrl)
    private Double price;              // Giá gốc (lấy từ course.price)
    private Double discountedPrice;    // Giá sau khi giảm (lấy từ course.discountedPrice)
    private Integer discountPercent;   // % giảm giá (lấy từ course.discountPercent)
    private boolean selected;          // Trạng thái được chọn

    // Các trường mới cho khóa học lập trình - KHỚP VỚI COURSE MODEL
    private String level;              // BEGINNER, INTERMEDIATE, ADVANCED (lấy từ course.level)
    private Integer duration;          // Thời lượng (giờ) (lấy từ course.duration)
    private String instructorName;     // Tên giảng viên (lấy từ course.instructorName)
    private Double rating;             // Đánh giá trung bình 0-5 (lấy từ course.rating)
    private Integer totalStudents;     // Số học viên đã đăng ký (lấy từ course.totalStudents)

    // BỎ quantity - khóa học không có số lượng
}

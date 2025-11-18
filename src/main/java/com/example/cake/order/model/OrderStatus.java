package com.example.cake.order.model;

public enum OrderStatus {
    UNCONFIRMED,    // Chưa xác nhận
    PENDING,        // Chờ thanh toán
    CONFIRMED,      // Đã xác nhận
    PAID,           // Đã thanh toán (khóa học online không cần shipping)
    ENROLLED,       // Đã kích hoạt khóa học
    COMPLETED,      // Hoàn thành khóa học
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}

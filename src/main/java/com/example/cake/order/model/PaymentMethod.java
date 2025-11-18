package com.example.cake.order.model;

public enum PaymentMethod {
    COD,                // Thanh toán khi nhận hàng
    BANK_TRANSFER,      // Chuyển khoản
    CREDIT_CARD,       // Thẻ tín dụng/ghi nợ
    E_WALLET,          // Ví điện tử
    ONLINE_PAYMENT     // Thanh toán trực tuyến (PayPal, Stripe, v.v.)

}

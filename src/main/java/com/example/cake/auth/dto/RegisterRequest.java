    package com.example.cake.auth.dto;
    import com.fasterxml.jackson.annotation.JsonProperty;
    import lombok.Data;
    import jakarta.validation.constraints.*;

    @Data
    public class RegisterRequest {

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;


        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
        private String password;


        @NotBlank(message = "Họ tên không được để trống")
        @JsonProperty(value = "fullname")
        private String fullname;


        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "0\\d{9}", message = "Số điện thoại không hợp lệ")
        private String phoneNumber;
    }
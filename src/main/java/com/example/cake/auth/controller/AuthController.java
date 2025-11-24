    package com.example.cake.auth.controller;


import com.example.cake.auth.dto.*;
import com.example.cake.auth.model.User;
import com.example.cake.auth.service.AuthService;
import com.example.cake.response.ResponseMessage;
import com.example.cake.user.dto.ForgetPasswordRequest;
import com.example.cake.user.dto.ResetPasswordRequest;
import com.example.cake.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseMessage<JwtResponse>> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshTokenFromCookie(request));
    }

    //login with gg
    @PostMapping("/google")
    public ResponseEntity<ResponseMessage<JwtResponse>> loginGoogle(
            @RequestBody Map<String, String> request,
            HttpServletResponse response) {
        String idToken = request.get("idToken");
        System.out.println("✅ ID Token từ FE trong Controller: " + idToken);
        return ResponseEntity.ok(authService.loginWithGoogle(idToken, response));
    }

    //Đăng ký tạo tài khoản user
    @PostMapping("/register")
    public ResponseEntity<ResponseMessage<Map<String, String>>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    //Gửi mã OTP tới email để confirm
    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseMessage<User>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<ResponseMessage<Map<String, String>>> forgetPassword(
            @Valid @RequestBody ForgetPasswordRequest request) {
        return ResponseEntity.ok(userService.sendFogetPassWord(request));
    }

    @PostMapping("/verify-otpPassword")
    public ResponseEntity<ResponseMessage<String>> verifyOtpPassword(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(userService.verifyOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseMessage<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<JwtResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ResponseMessage<Map<String, String>>> resendOtp(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        return ResponseEntity.ok(authService.resendOtp(token));
    }
}

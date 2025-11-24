package com.example.cake.auth.service;

import com.example.cake.auth.dto.*;

import com.example.cake.auth.model.AutheProvider;
import com.example.cake.auth.model.User;
import com.example.cake.auth.model.UserPrincipal;
import com.example.cake.auth.repository.UserRepository;
import com.example.cake.response.ResponseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FirebaseService firebaseService;
    private final EmailValidationService emailValidationService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService otpRedisService;
    private final JwtService jwtService;

    //         refreshToken khi accen hết hạn
    public ResponseMessage<JwtResponse> refreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return new ResponseMessage<>(false, "Không tìm thấy cookie!", null);
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            return new ResponseMessage<>(false, "Refresh token không có trong cookie!", null);
        }

        // Lấy email từ Redis theo refresh token
        String email = otpRedisService.getEmailFromRefreshToken(refreshToken);
        if (email == null) {
            return new ResponseMessage<>(false, "Token không hợp lệ hoặc đã hết hạn!", null);
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ResponseMessage<>(false, "Không tìm thấy người dùng!", null);
        }

        User user = userOptional.get();
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );

        String newAccessToken = jwtService.generateAccessToken(userPrincipal);
        return new ResponseMessage<>(true, "Làm mới access token thành công!", new JwtResponse(newAccessToken));
    }



    // đăng ký tạo tài khoản user
    public ResponseMessage<Map<String, String>> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ResponseMessage<>(false, "Email đã tồn tại!", null);
        }
        // tạo OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        // Tạo token
        String token = UUID.randomUUID().toString();

        //Tạo JSON chứa thông tin user + otp
        String jsonData = String.format("""
        {
          "email": "%s",
          "password": "%s",
          "fullname": "%s",
          "phoneNumber": "%s",
          "otp": "%s"
        }
        
        """, request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullname(),
                request.getPhoneNumber(),
                otp
        );

        // Validate email format and existence (with fallback)
        try {
            if (!emailValidationService.isValidEmail(request.getEmail())) {
                return new ResponseMessage<>(false, "Email không tồn tại hoặc không hợp lệ!", null);
            }
        } catch (Exception e) {
            // Fallback: if email validation API fails, continue with basic validation
            System.err.println("⚠️ Email validation service failed: " + e.getMessage());
        }

        emailService.sendOtpEmail(request.getEmail(), otp);
        otpRedisService.saveOtp(token, jsonData, 5);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("email", request.getEmail());
        data.put("message", "OTP đã được gửi đến email. Vui lòng kiểm tra hộp thư (bao gồm cả spam).");
        return new ResponseMessage<>(true, "OTP đã gửi về email! Hiệu lực 5 phút.",data);
    }


    public ResponseMessage<JwtResponse> loginWithGoogle(String idToken, HttpServletResponse response) {
        try {
            FirebaseToken firebaseToken = firebaseService.verifyToken(idToken);

            String email = firebaseToken.getEmail();
            String name = firebaseToken.getName();
            String picture = firebaseToken.getPicture();

            Optional<User> optional = userRepository.findByEmail(email);
            User user;

            if (optional.isPresent()) {
                user = optional.get();

                if (!user.isActive()) {
                    return new ResponseMessage<>(false, "Tài khoản đã bị chặn quyền truy cập hoạt động!", null);
                }
            } else {
                // Tạo user mới nếu chưa tồn tại
                user = User.builder()
                        .email(email)
                        .fullname(name)
                        .avatarUrl(picture)
                        .role("USER")
                        .active(true)
                        .createdAt(LocalDate.now())
                        .authProvider(AutheProvider.GOOGLE)
                        .build();
                userRepository.save(user);
            }

            UserPrincipal userPrincipal = new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(), // có thể null, không ảnh hưởng
                    user.getRole(),
                    user.isActive()
            );

            // Tạo JWT + refreshToken + Cookie
            String accessToken = jwtService.generateAccessToken(userPrincipal);
            String refreshToken = UUID.randomUUID().toString();
            otpRedisService.saveRefreshToken(user.getEmail(), refreshToken, 10080); // 7 ngày

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return new ResponseMessage<>(true, "Đăng nhập Google thành công!", new JwtResponse(accessToken));
        } catch (FirebaseAuthException e) {
            return new ResponseMessage<>(false, "Token Google không hợp lệ!", null);
        }
    }


    public ResponseMessage<JwtResponse> login(LoginRequest request, HttpServletResponse response) {
        Optional<User> optional = userRepository.findByEmail(request.getEmail());

        if (optional.isEmpty()) {
            return new ResponseMessage<>(false, "Email không tồn tại!", null);
        }

        User user = optional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ResponseMessage<>(false, "Mật khẩu không đúng!", null);
        }
        if (!user.isActive()) {
            return new ResponseMessage<>(false, "Tài khoản đã bị chặn quyền truy cập hoạt động!", null);
        }


        // Chuyển User → UserPrincipal
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = UUID.randomUUID().toString();
        otpRedisService.saveRefreshToken(request.getEmail(), refreshToken, 10080); // 7 ngày

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        response.addCookie(cookie);

        return new ResponseMessage<>(true, "Đăng nhập thành công", new JwtResponse(accessToken));
    }



    // Resend OTP với token cũ
    public ResponseMessage<Map<String, String>> resendOtp(String token) {
        String json = otpRedisService.getOtp(token);

        if (json == null) {
            return new ResponseMessage<>(false, "Token không hợp lệ hoặc đã hết hạn! Vui lòng đăng ký lại.", null);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            String email = node.get("email").asText();

            // Tạo OTP mới
            String newOtp = String.format("%06d", new java.util.Random().nextInt(999999));

            // Cập nhật OTP mới vào JSON
            String updatedJson = String.format("""
            {
              "email": "%s",
              "password": "%s",
              "fullname": "%s",
              "phoneNumber": "%s",
              "otp": "%s"
            }
            """, node.get("email").asText(),
                    node.get("password").asText(),
                    node.get("fullname").asText(),
                    node.get("phoneNumber").asText(),
                    newOtp
            );

            // Gửi OTP mới
            emailService.sendOtpEmail(email, newOtp);
            otpRedisService.saveOtp(token, updatedJson, 5);

            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("email", email);
            data.put("message", "OTP mới đã được gửi đến email.");

            return new ResponseMessage<>(true, "OTP mới đã được gửi! Hiệu lực 5 phút.", data);
        } catch (Exception e) {
            return new ResponseMessage<>(false, "Lỗi khi gửi lại OTP!", null);
        }
    }

    // gửi mã otp xác nhận đăng ký tài khoản
    public ResponseMessage<User> verifyOtp(VerifyOtpRequest request) {
        String json = otpRedisService.getOtp(request.getToken());

        if (json == null) {
            return new ResponseMessage<>(false, "Token không hợp lệ hoặc đã hết hạn! Vui lòng đăng ký lại.", null);
        }

        // Chuyển từ JSON về object bằng Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            String otpSaved = node.get("otp").asText();
            if (!otpSaved.equals(request.getOtp())) {
                return new ResponseMessage<>(false, "Mã OTP không đúng!", null);
            }

            String email = node.get("email").asText();

            // Kiểm tra email đã tồn tại chưa (trong trường hợp đã verify rồi)
            if (userRepository.findByEmail(email).isPresent()) {
                return new ResponseMessage<>(false, "Email đã được đăng ký! Vui lòng đăng nhập.", null);
            }

            // Tạo user từ dữ liệu trong JSON
            User user = User.builder()
                    .email(email)
                    .password(node.get("password").asText())
                    .fullname(node.get("fullname").asText())
                    .phoneNumber(node.get("phoneNumber").asText())
                    .role("USER")
                    .active(true)
                    .createdAt(LocalDate.now())
                    .authProvider(AutheProvider.LOCAL)
                    .build();

            User saved = userRepository.save(user);
            otpRedisService.deleteOtp(request.getToken());

            return new ResponseMessage<>(true, "Xác minh OTP thành công! Tài khoản đã được tạo.", saved);
        } catch (Exception e) {
            return new ResponseMessage<>(false, "Lỗi xử lý dữ liệu OTP!", null);
        }
    }

}

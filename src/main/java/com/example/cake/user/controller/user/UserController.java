package com.example.cake.user.controller.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import com.example.cake.auth.model.UserPrincipal;
import com.example.cake.auth.model.User;
import com.example.cake.response.ResponseMessage;
import com.example.cake.user.dto.ChangePasswordRequest;
import com.example.cake.user.dto.UpdateUserRequest;
import com.example.cake.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.util.PublicSuffixList;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResponseMessage<String>> changePassWord(@RequestBody ChangePasswordRequest changePasswordRequest,
          @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        System.out.println("✅ ID  passs word zô roi  " + changePasswordRequest.getPassword()+ changePasswordRequest.getNewPassword());
        return ResponseEntity.ok(userService.changePassword(userPrincipal.getEmail(),changePasswordRequest));
    }


    @GetMapping("/find-userId")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResponseMessage<Optional<User>>> findUserById(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(userService.getUserById(userPrincipal.getId()));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResponseMessage<String>> deleteUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.deleteUserbyId(id));
    }


    @PutMapping("/update-user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseMessage<User>> updateUser(
            @RequestPart("request") String rawJson,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            UpdateUserRequest request = mapper.readValue(rawJson, UpdateUserRequest.class);
            return ResponseEntity.ok(userService.updateUser(userPrincipal.getEmail(), request, avatarFile));
        } catch (Exception e) {
            log.error("❌ Lỗi parse JSON từ Postman", e);
            return ResponseEntity.badRequest().body(new ResponseMessage<>(false, "Lỗi parse JSON", null));
        }
    }
}

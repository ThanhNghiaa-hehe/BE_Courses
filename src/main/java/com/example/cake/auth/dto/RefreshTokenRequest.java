package com.example.cake.auth.dto;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String email;
    private String refreshToken;
}


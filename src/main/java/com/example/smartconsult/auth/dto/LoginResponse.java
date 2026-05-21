package com.example.smartconsult.auth.dto;

import com.example.smartconsult.user.dto.UserProfileResponse;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String tokenType;

    private long expiresInSeconds;

    private UserProfileResponse user;

    private boolean registered;
}

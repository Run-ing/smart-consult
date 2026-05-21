package com.example.smartconsult.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String phone;

    private String smsCode;
}

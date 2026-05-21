package com.example.smartconsult.auth.dto;

import lombok.Data;

@Data
public class SmsCodeResponse {

    private String phone;

    private String mockCode;

    private long expiresInSeconds;
}

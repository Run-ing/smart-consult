package com.example.smartconsult.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserHealthProfileRequest {

    private String sex;

    private LocalDate birthDate;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private BigDecimal waistCm;
}

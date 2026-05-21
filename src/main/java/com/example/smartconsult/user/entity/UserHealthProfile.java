package com.example.smartconsult.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_health_profile")
public class UserHealthProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sex;

    private LocalDate birthDate;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private BigDecimal waistCm;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

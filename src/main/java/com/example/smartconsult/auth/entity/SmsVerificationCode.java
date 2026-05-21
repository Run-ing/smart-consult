package com.example.smartconsult.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_verification_code")
public class SmsVerificationCode {

    public static final String SCENE_LOGIN = "LOGIN";
    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_EXPIRED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String code;

    private String scene;

    private Integer status;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private Integer mockSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

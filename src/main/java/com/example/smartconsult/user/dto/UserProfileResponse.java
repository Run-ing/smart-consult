package com.example.smartconsult.user.dto;

import com.example.smartconsult.user.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime lastLoginTime;

    public static UserProfileResponse from(SysUser user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setLastLoginTime(user.getLastLoginTime());
        return response;
    }
}

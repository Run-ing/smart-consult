package com.example.smartconsult.user.dto;

import com.example.smartconsult.user.entity.UserHealthProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Data
public class UserHealthProfileResponse {

    private Long id;

    private Long userId;

    private String sex;

    private LocalDate birthDate;

    private Integer age;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private BigDecimal waistCm;

    public static UserHealthProfileResponse from(UserHealthProfile profile) {
        UserHealthProfileResponse response = new UserHealthProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setSex(profile.getSex());
        response.setBirthDate(profile.getBirthDate());
        response.setAge(Period.between(profile.getBirthDate(), LocalDate.now()).getYears());
        response.setHeightCm(profile.getHeightCm());
        response.setWeightKg(profile.getWeightKg());
        response.setWaistCm(profile.getWaistCm());
        return response;
    }
}

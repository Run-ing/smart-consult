package com.example.smartconsult.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.exception.BusinessException;
import com.example.smartconsult.user.dto.UserHealthProfileRequest;
import com.example.smartconsult.user.dto.UserHealthProfileResponse;
import com.example.smartconsult.user.entity.UserHealthProfile;
import com.example.smartconsult.user.mapper.UserHealthProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UserHealthProfileService {

    private static final Set<String> SUPPORTED_SEX = Set.of("MALE", "FEMALE");

    private final UserHealthProfileMapper userHealthProfileMapper;

    public UserHealthProfileService(UserHealthProfileMapper userHealthProfileMapper) {
        this.userHealthProfileMapper = userHealthProfileMapper;
    }

    public boolean existsByUserId(Long userId) {
        return userHealthProfileMapper.selectCount(new LambdaQueryWrapper<UserHealthProfile>()
                .eq(UserHealthProfile::getUserId, userId)) > 0;
    }

    public UserHealthProfileResponse getByUserId(Long userId) {
        UserHealthProfile profile = findByUserId(userId);
        if (profile == null) {
            throw new BusinessException(404, "用户画像不存在");
        }
        return UserHealthProfileResponse.from(profile);
    }

    @Transactional
    public UserHealthProfileResponse save(Long userId, UserHealthProfileRequest request) {
        validate(request);
        LocalDateTime now = LocalDateTime.now();
        UserHealthProfile profile = findByUserId(userId);
        if (profile == null) {
            profile = new UserHealthProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(now);
        }
        profile.setSex(request.getSex().trim().toUpperCase());
        profile.setBirthDate(request.getBirthDate());
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setWaistCm(request.getWaistCm());
        profile.setUpdatedAt(now);

        if (profile.getId() == null) {
            userHealthProfileMapper.insert(profile);
        } else {
            userHealthProfileMapper.updateById(profile);
        }
        return UserHealthProfileResponse.from(profile);
    }

    private UserHealthProfile findByUserId(Long userId) {
        return userHealthProfileMapper.selectOne(new LambdaQueryWrapper<UserHealthProfile>()
                .eq(UserHealthProfile::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void validate(UserHealthProfileRequest request) {
        if (request == null) {
            throw new BusinessException(400, "建档信息不能为空");
        }
        String sex = request.getSex() == null ? "" : request.getSex().trim().toUpperCase();
        if (!SUPPORTED_SEX.contains(sex)) {
            throw new BusinessException(400, "性别只能选择男性或女性");
        }
        if (request.getBirthDate() == null || request.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException(400, "出生日期不正确");
        }
        requireRange(request.getHeightCm(), new BigDecimal("50"), new BigDecimal("250"), "身高");
        requireRange(request.getWeightKg(), new BigDecimal("10"), new BigDecimal("300"), "体重");
        if (request.getWaistCm() != null) {
            requireRange(request.getWaistCm(), new BigDecimal("30"), new BigDecimal("250"), "腰围");
        }
    }

    private void requireRange(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value == null || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException(400, fieldName + "不在合理范围内");
        }
    }
}

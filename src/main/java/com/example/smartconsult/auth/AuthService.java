package com.example.smartconsult.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.auth.dto.LoginRequest;
import com.example.smartconsult.auth.dto.LoginResponse;
import com.example.smartconsult.auth.dto.SmsCodeRequest;
import com.example.smartconsult.auth.dto.SmsCodeResponse;
import com.example.smartconsult.auth.entity.SmsVerificationCode;
import com.example.smartconsult.auth.mapper.SmsVerificationCodeMapper;
import com.example.smartconsult.exception.BusinessException;
import com.example.smartconsult.user.UserService;
import com.example.smartconsult.user.dto.UserProfileResponse;
import com.example.smartconsult.user.entity.SysUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern CHINA_MAINLAND_PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private final SmsVerificationCodeMapper smsVerificationCodeMapper;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long smsCodeExpiresInSeconds;
    private final boolean smsMockEnabled;

    public AuthService(
            SmsVerificationCodeMapper smsVerificationCodeMapper,
            UserService userService,
            JwtTokenService jwtTokenService,
            @Value("${app.sms.code-expires-in-seconds}") long smsCodeExpiresInSeconds,
            @Value("${app.sms.mock-enabled}") boolean smsMockEnabled) {
        this.smsVerificationCodeMapper = smsVerificationCodeMapper;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.smsCodeExpiresInSeconds = smsCodeExpiresInSeconds;
        this.smsMockEnabled = smsMockEnabled;
    }

    @Transactional
    public SmsCodeResponse sendSmsCode(SmsCodeRequest request) {
        String phone = normalizeAndValidatePhone(request.getPhone());
        LocalDateTime now = LocalDateTime.now();
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        SmsVerificationCode verificationCode = new SmsVerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setScene(SmsVerificationCode.SCENE_LOGIN);
        verificationCode.setStatus(SmsVerificationCode.STATUS_UNUSED);
        verificationCode.setExpiresAt(now.plusSeconds(smsCodeExpiresInSeconds));
        verificationCode.setMockSent(smsMockEnabled ? 1 : 0);
        verificationCode.setCreatedAt(now);
        verificationCode.setUpdatedAt(now);
        smsVerificationCodeMapper.insert(verificationCode);

        SmsCodeResponse response = new SmsCodeResponse();
        response.setPhone(phone);
        response.setExpiresInSeconds(smsCodeExpiresInSeconds);
        if (smsMockEnabled) {
            response.setMockCode(code);
        }
        return response;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String phone = normalizeAndValidatePhone(request.getPhone());
        String smsCode = validateSmsCode(request.getSmsCode());
        SmsVerificationCode verificationCode = findValidCode(phone, smsCode);

        boolean registered = userService.findByPhone(phone) == null;
        SysUser user = userService.getOrCreateByPhone(phone);
        LocalDateTime now = LocalDateTime.now();
        userService.updateLastLoginTime(user.getId(), now);
        user.setLastLoginTime(now);

        verificationCode.setStatus(SmsVerificationCode.STATUS_USED);
        verificationCode.setUsedAt(now);
        verificationCode.setUpdatedAt(now);
        smsVerificationCodeMapper.updateById(verificationCode);

        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenService.createToken(user));
        response.setTokenType("Bearer");
        response.setExpiresInSeconds(jwtTokenService.getExpiresInSeconds());
        response.setUser(UserProfileResponse.from(user));
        response.setRegistered(registered);
        return response;
    }

    private SmsVerificationCode findValidCode(String phone, String smsCode) {
        SmsVerificationCode verificationCode = smsVerificationCodeMapper.selectOne(
                new LambdaQueryWrapper<SmsVerificationCode>()
                        .eq(SmsVerificationCode::getPhone, phone)
                        .eq(SmsVerificationCode::getCode, smsCode)
                        .eq(SmsVerificationCode::getScene, SmsVerificationCode.SCENE_LOGIN)
                        .eq(SmsVerificationCode::getStatus, SmsVerificationCode.STATUS_UNUSED)
                        .orderByDesc(SmsVerificationCode::getId)
                        .last("LIMIT 1"));

        if (verificationCode == null) {
            throw new BusinessException(4001, "验证码错误或已使用");
        }
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCode.setStatus(SmsVerificationCode.STATUS_EXPIRED);
            verificationCode.setUpdatedAt(LocalDateTime.now());
            smsVerificationCodeMapper.updateById(verificationCode);
            throw new BusinessException(4002, "验证码已过期");
        }
        return verificationCode;
    }

    private String normalizeAndValidatePhone(String phone) {
        if (phone == null || !CHINA_MAINLAND_PHONE.matcher(phone.trim()).matches()) {
            throw new BusinessException(400, "手机号格式错误");
        }
        return phone.trim();
    }

    private String validateSmsCode(String smsCode) {
        if (smsCode == null || !smsCode.trim().matches("^\\d{6}$")) {
            throw new BusinessException(400, "验证码格式错误");
        }
        return smsCode.trim();
    }
}

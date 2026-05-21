package com.example.smartconsult.auth;

import com.example.smartconsult.auth.dto.LoginRequest;
import com.example.smartconsult.auth.dto.LoginResponse;
import com.example.smartconsult.auth.dto.SmsCodeRequest;
import com.example.smartconsult.auth.dto.SmsCodeResponse;
import com.example.smartconsult.common.Result;
import com.example.smartconsult.user.UserHealthProfileService;
import com.example.smartconsult.user.UserService;
import com.example.smartconsult.user.dto.UserProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserHealthProfileService userHealthProfileService;

    public AuthController(AuthService authService, UserService userService, UserHealthProfileService userHealthProfileService) {
        this.authService = authService;
        this.userService = userService;
        this.userHealthProfileService = userHealthProfileService;
    }

    @PostMapping("/sms-code")
    public Result<SmsCodeResponse> sendSmsCode(@RequestBody SmsCodeRequest request) {
        return Result.success(authService.sendSmsCode(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserProfileResponse> me() {
        CurrentUser currentUser = CurrentUserContext.getRequired();
        return Result.success(UserProfileResponse.from(
                userService.findById(currentUser.id()),
                userHealthProfileService.existsByUserId(currentUser.id())));
    }
}

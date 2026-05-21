package com.example.smartconsult.user;

import com.example.smartconsult.auth.CurrentUser;
import com.example.smartconsult.auth.CurrentUserContext;
import com.example.smartconsult.common.Result;
import com.example.smartconsult.user.dto.UserHealthProfileRequest;
import com.example.smartconsult.user.dto.UserHealthProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/profile")
public class UserHealthProfileController {

    private final UserHealthProfileService userHealthProfileService;

    public UserHealthProfileController(UserHealthProfileService userHealthProfileService) {
        this.userHealthProfileService = userHealthProfileService;
    }

    @GetMapping
    public Result<UserHealthProfileResponse> getProfile() {
        CurrentUser currentUser = CurrentUserContext.getRequired();
        return Result.success(userHealthProfileService.getByUserId(currentUser.id()));
    }

    @PostMapping
    public Result<UserHealthProfileResponse> saveProfile(@RequestBody UserHealthProfileRequest request) {
        CurrentUser currentUser = CurrentUserContext.getRequired();
        return Result.success(userHealthProfileService.save(currentUser.id(), request));
    }
}

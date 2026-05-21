package com.example.smartconsult.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetCurrentUserProfileRequest {

    @JsonProperty("user_id")
    private Long userId;
}

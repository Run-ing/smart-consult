package com.example.smartconsult.controller;

import com.example.smartconsult.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lizhifu
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<String> healthCheck() {
        return Result.success("OK");
    }
}

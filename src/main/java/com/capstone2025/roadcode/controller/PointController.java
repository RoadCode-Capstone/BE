package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/attendance/check")
    public ApiResponse<?> checkAttendance(Authentication authentication) {

        String email = authentication.getName();
        String message = pointService.checkAttendance(email);
        return ApiResponse.successWithMessage(message);
    }
}

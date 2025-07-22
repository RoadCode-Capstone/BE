package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/my")
    public ApiResponse<?> getPointHistory(Authentication authentication,
                                          @RequestParam String groupBy,
                                          @RequestParam String start,
                                          @RequestParam String end) {

        String email = authentication.getName();
        if("DATE".equalsIgnoreCase(groupBy)) {
            return ApiResponse.success(pointService.getPointHistoryByDate(email, start, end));
        } else if("TYPE".equalsIgnoreCase(groupBy)){
            return ApiResponse.success(pointService.getPointHistoryByType(email, start, end));
        } else {
            return ApiResponse.error(ErrorCode.INVALID_PARAMETER_GROUP_BY);
        }
    }
}

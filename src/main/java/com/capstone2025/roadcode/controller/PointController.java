package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.MemberPointRank;
import com.capstone2025.roadcode.dto.MemberPointRanking;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 출석 체크
    @PostMapping("/attendance/check")
    public ApiResponse<String> checkAttendance(Authentication authentication) {

        String email = authentication.getName();
        String message = pointService.checkAttendance(email);
        return ApiResponse.successWithMessage(message);
    }

    // 포인트 내역 목록 조회
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

    // 포인트 순위(랭킹) 조회
    @GetMapping("/ranking")
    public ApiResponse<MemberPointRanking> getPointRanking(Authentication authentication,
                                                           @RequestParam String start,
                                                           @RequestParam String end) {
        String email = authentication.getName();
        return ApiResponse.success(pointService.getPointRanking(email, start, end));
    }
}

package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.ProblemInfoResponse;
import com.capstone2025.roadcode.dto.ProblemResponse;
import com.capstone2025.roadcode.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    // 문제 조회
    @GetMapping("/{problemId}")
    public ApiResponse<ProblemInfoResponse> getProblemInfo(@PathVariable Long problemId) {
        return ApiResponse.success(problemService.getProblemInfo(problemId));
    }

    // 문제 목록 전체 조회
    @GetMapping
    public ApiResponse<List<ProblemResponse>> getProblems(
            @RequestParam(required = false) List<Long> ids
    ) {
        List<ProblemResponse> response;

        if (ids != null && !ids.isEmpty()) {
            response = problemService.getProblemsByIdsWithTags(ids);
        } else {
            response = problemService.getAllProblemsWithTags();
        }

        return ApiResponse.success(response);
    }
}

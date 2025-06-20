package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.RoadmapInfoResponse;
import com.capstone2025.roadcode.dto.RoadmapProblemResponse;
import com.capstone2025.roadcode.dto.RoadmapRequest;
import com.capstone2025.roadcode.dto.RoadmapResponse;
import com.capstone2025.roadcode.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;

    // 로드맵 생성
    @PostMapping
    public ApiResponse<String> createRoadmap(@RequestBody RoadmapRequest request, Authentication authentication) {

        String email = authentication.getName();
        roadmapService.createRoadmap(request, email);

        return ApiResponse.successWithMessage("로드맵이 생성되었습니다.");
    }

    // 회원이 가진 로드맵 목록 조회
    @GetMapping("/my")
    public ApiResponse<List<RoadmapResponse>> getRoadmaps(Authentication authentication) {
        String email = authentication.getName();

        return ApiResponse.success(roadmapService.getRoadmaps(email));
    }

    // 로드맵 정보 조회
    @GetMapping("/{roadmapId}")
    public ApiResponse<RoadmapInfoResponse> getRoadmapInfo(@PathVariable Long roadmapId) {
        return ApiResponse.success(roadmapService.getRoadmapInfo(roadmapId));
    }

    // 로드맵 문제 목록 조회 (상세 문제 조회는 id 사용해서- ProblemController)
    @GetMapping("/{roadmapId}/problems")
    public ApiResponse<List<RoadmapProblemResponse>> getRoadmapProblems(@PathVariable Long roadmapId) {
        List<RoadmapProblemResponse> problems = roadmapService.getRoadmapProblems(roadmapId);
        return ApiResponse.success(problems);
    }

    // 로드맵 삭제 (테스트/시연)
    @DeleteMapping("/{roadmapId}")
    public ApiResponse<String> deleteRoadmap(@PathVariable Long roadmapId, Authentication authentication) {
        String email = authentication.getName();
        roadmapService.deleteRoadmap(roadmapId, email);

        return ApiResponse.successWithMessage("로드맵이 생성되었습니다.");
    }


}

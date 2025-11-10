package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.entity.RoadmapStatus;
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
    public ApiResponse<ReturnIdResponse> createRoadmap(@RequestBody RoadmapCreateRequest request, Authentication authentication) {

        String email = authentication.getName();

        return ApiResponse.success(roadmapService.createRoadmap(request, email));
    }

    // 회원이 가진 로드맵 목록 조회
    @GetMapping("/my")
    public ApiResponse<RoadmapListResponse> getRoadmaps(
            Authentication authentication,
            @RequestParam(value = "statusList", required = false) List<RoadmapStatus> statusList) {
        String email = authentication.getName();
        return ApiResponse.success(roadmapService.getRoadmaps(email, statusList));
    }

    // 로드맵 정보 조회
    @GetMapping("/{roadmapId}")
    public ApiResponse<RoadmapInfoResponse> getRoadmapInfo(@PathVariable Long roadmapId, Authentication authentication) {

        String email = authentication.getName();
        return ApiResponse.success(roadmapService.getRoadmapInfo(email, roadmapId));
    }

    // 로드맵 문제 목록 조회 (상세 문제 조회는 id 사용해서- ProblemController)
    @GetMapping("/{roadmapId}/problems")
    public ApiResponse<RoadmapProblemListResponse> getRoadmapProblems(@PathVariable Long roadmapId, Authentication authentication) {

        String email = authentication.getName();
        RoadmapProblemListResponse problems = roadmapService.getRoadmapProblems(email, roadmapId);
        return ApiResponse.success(problems);
    }

    // 로드맵 삭제 (테스트/시연)
    @DeleteMapping("/{roadmapId}")
    public ApiResponse<Void> deleteRoadmap(@PathVariable Long roadmapId, Authentication authentication) {

        String email = authentication.getName();
        roadmapService.deleteRoadmap(roadmapId, email);

        return ApiResponse.successWithMessage("로드맵이 삭제되었습니다.");
    }

    // 로드맵 포기
    @PostMapping("/{roadmapId}/give-up")
    public ApiResponse<?> giveUpRoadmap(@PathVariable Long roadmapId, Authentication authentication) {

        String email = authentication.getName();
        roadmapService.giveUpRoadmap(roadmapId, email);

        return ApiResponse.successWithMessage("로드맵 포기 상태로 변경");
    }

    // 로드맵 문제 추가(개념 강화 문제)
    // roadmapId, currentProblemId, authentication
    @PostMapping("/{roadmapId}/concept-problem")
    public ApiResponse<RoadmapInfoResponse> addConceptProblem(@PathVariable Long roadmapId,
                                            Authentication authentication,
                                            @RequestBody AddConceptProblemRequest request) {

        String email = authentication.getName();
        roadmapService.addConceptProblem(roadmapId, email, request);

        return ApiResponse.success(roadmapService.getRoadmapInfo(email, roadmapId));
    }

    // 로드맵 문제 추가(추천 문제)
    // roadmapId, authentication
    @PostMapping("/{roadmapId}/recommend-problems")
    public ApiResponse<RoadmapInfoResponse> addRecommendProblem(@PathVariable Long roadmapId,
                                                              Authentication authentication) {

        String email = authentication.getName();
        roadmapService.addRecommendProblems(roadmapId, email);

        return ApiResponse.success(roadmapService.getRoadmapInfo(email, roadmapId));
    }



}

package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1") // 수정 여부 결정(solution?)
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // 풀이 제출 (수정) problemId 받아서 실행
    @PostMapping("/problems/{problemId}/submission")
    public ApiResponse<SubmitSolutionResponse> submitSolution(@PathVariable Long problemId, @RequestBody SubmitSolutionRequest request, Authentication authentication) {
        // 사용자 id, 언어, 코드를 인자로 받음
        String email = authentication.getName();
        return ApiResponse.success(submissionService.submitSolution(email, problemId, request));
    }

    // 레벨 테스트 문제 풀이 제출
    @PostMapping("/level-test/submissions")
    public ApiResponse<LevelTestResultResponse> submitLevelTest(@RequestBody LevelTestSubmissionsRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(submissionService.submitLevelTest(email, request));
    }


//    // 특정 사용자 풀이 조회
//    @GetMapping()
//    public ApiResponse<?> getSolution() {
//        return ApiResponse.successWithMessage("");
//    }
//
    // 다른 사람 풀이 목록 조회
    @GetMapping("problem/{problemId}/submissions/success")
    public ApiResponse<OtherMemberSubmissionListResponse> getOtherSuccessfulSubmissions(Authentication authentication, @PathVariable Long problemId) {

        String email = authentication.getName();
        return ApiResponse.success(submissionService.getOtherSuccessfulSubmissions(email, problemId));
    }
//
//    // 내가 푼 풀이 목록 조회
//    @GetMapping()
//    public ApiResponse<?> getMySolutions() {
//        return ApiResponse.successWithMessage("");
//    }
//
//    // 내가 푼 문제 목록 조회
//    @GetMapping()
//    public ApiResponse<?> getMySolvedProblems() {
//        return ApiResponse.successWithMessage("");
//    }
//
//    // 문제 가져오기
//    @GetMapping()
//    public ApiResponse<?> getProblem() {
//        return ApiResponse.successWithMessage("");
//    }

}

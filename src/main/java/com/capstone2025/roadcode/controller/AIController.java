package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.LevelTestRequest;
import com.capstone2025.roadcode.dto.RoadmapRequest;
import com.capstone2025.roadcode.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AIController {

    private final OpenAIService openAiService;

    @PostMapping("/roadmap")
    public ApiResponse<List<Integer>> generateRoadmap(@RequestBody RoadmapRequest request) {
        List<Integer> result = openAiService.createRoadmap(
                request.getType(),
                request.getAlgorithm(),
                request.getDailyGoal(),
                request.getLevelTestResult()
        );
        return ApiResponse.success(result);
    }

    @PostMapping("/level-test")
    public ApiResponse<List<Integer>> generateLevelTest(@RequestBody LevelTestRequest request) {
        List<Integer> result = openAiService.createLevelTest(
                request.getType(),
                request.getAlgorithm()
        );
        return ApiResponse.success(result);
    }
}

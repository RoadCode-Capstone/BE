package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.LevelTestCreateRequest;
import com.capstone2025.roadcode.dto.LevelTestCreateResponse;
import com.capstone2025.roadcode.entity.RoadmapType;
import com.capstone2025.roadcode.service.LevelTestService;
import com.capstone2025.roadcode.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/level-test")
public class LevelTestController {

    private final LevelTestService levelTestService;

    @PostMapping()
    public ApiResponse<LevelTestCreateResponse> generateLevelTest(@RequestBody LevelTestCreateRequest request) {
        return ApiResponse.success(levelTestService.createLevelTest(request));
    }
}

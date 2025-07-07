package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.LevelTestCreateRequest;
import com.capstone2025.roadcode.dto.LevelTestCreateResponse;
import com.capstone2025.roadcode.entity.RoadmapType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelTestService {

    private final OpenAIService openAIService;

    public LevelTestCreateResponse createLevelTest(LevelTestCreateRequest request) {

        List<Long> problemIds = openAIService.createLevelTest(
                RoadmapType.fromString(request.getType()),
                request.getCategory());

        return new LevelTestCreateResponse(problemIds);
    }
}

package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.RoadmapType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapInfoResponse {

    private Long roadmapId;
    private String title;
    private RoadmapType type;
    private String language;
    private String algorithm;
    private RoadmapProblemResponse currentProblem;
    private int levelTestResult;
    private int dailyGoal;

}

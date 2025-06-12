package com.capstone2025.roadcode.dto;

import lombok.Getter;

@Getter
public class RoadmapRequest {
    private String type;
    private String algorithm;
    private int dailyGoal;
    private int levelTestResult;
}

package com.capstone2025.roadcode.dto;

import lombok.Getter;

@Getter
public class RoadmapCreateRequest {
    private String type;
    private String language;
    private String algorithm;
    private int levelTestResult;
    private int dailyGoal;
}

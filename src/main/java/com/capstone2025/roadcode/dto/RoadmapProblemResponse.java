package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.RoadmapProblem;
import com.capstone2025.roadcode.entity.RoadmapProblemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapProblemResponse {
    private Long roadmapProblemId;
    private Long problemId;
    private int order;
    private RoadmapProblemStatus status;

    public static RoadmapProblemResponse from(RoadmapProblem rp) {
        return new RoadmapProblemResponse(
                rp.getId(),
                rp.getProblem().getId(),
                rp.getSequence(),
                rp.getStatus()
        );
    }
}

package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.entity.RoadmapStatus;
import com.capstone2025.roadcode.entity.RoadmapType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapResponse {
    private Long roadmapId;
    private String title;
    private RoadmapType type;
    private LanguageType language;
    private String algorithm;
    private RoadmapStatus status;
}

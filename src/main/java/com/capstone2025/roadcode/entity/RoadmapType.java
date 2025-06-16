package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;

import java.util.Arrays;

public enum RoadmapType {
    Algorithm,
    Language;

    public static RoadmapType fromString(String type) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ROADMAP_TYPE));
    }
}

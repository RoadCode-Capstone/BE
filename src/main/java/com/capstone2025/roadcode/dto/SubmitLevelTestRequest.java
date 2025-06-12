package com.capstone2025.roadcode.dto;

import lombok.Getter;

@Getter
public class SubmitLevelTestRequest {
    private Long problemId;
    private String language;
    private String sourceCode;
}

package com.capstone2025.roadcode.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitSolutionRequest {
    private String language;
    // (수정) 자료형 더 크게 변경 필요?
    private String sourceCode;
}

package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubmitSolutionRequest {
    private Long roadmapId; // 로드맵 아이디 (필수x)
    private Long roadmapProblemId; // 로드맵 문제 아이디 (필수x)
    private String language; // 선택한 언어
    private String sourceCode; // 풀이 코드

    public SubmitSolutionRequest(String language, String sourceCode) {
        this.language = language;
        this.sourceCode = sourceCode;
    }
}

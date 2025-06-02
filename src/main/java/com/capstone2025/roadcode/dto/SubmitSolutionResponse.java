package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitSolutionResponse {
    // (수정 필요) 테스트 케이스 실행 후, 응답 결과로 뭘 받아올지
    private boolean allPassed;
    private List<TestcaseResult> testcaseResults = new ArrayList<>();
    // private long executionTime;
}

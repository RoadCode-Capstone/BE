package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TestcaseResult {
    private boolean passed;
    private String message;
    // private Long executionTime;
    // 메모리도 추가
}

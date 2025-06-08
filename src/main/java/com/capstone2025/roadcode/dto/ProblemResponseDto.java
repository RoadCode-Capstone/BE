package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProblemResponseDto {
    private Long id;
    private Integer contestId;
    private String index;
    private String name;
    private Integer rating;
    private String description;
    private String inputDescription;
    private String outputDescription;
    private String timeLimit;
    private String memoryLimit;
    private String url;
    private LocalDateTime createdAt;
    private List<String> tags;
}

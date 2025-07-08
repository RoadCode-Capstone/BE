package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private Long problemId;
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
    //private LocalDateTime createdAt;
    private AllTagsResponse tags;

    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getContestId(),
                problem.getIndex(),
                problem.getName(),
                problem.getRating(),
                problem.getDescription(),
                problem.getInputDescription(),
                problem.getOutputDescription(),
                problem.getTimeLimit(),
                problem.getMemoryLimit(),
                problem.getUrl(),
                //problem.getCreatedAt(),
                AllTagsResponse.from(problem)
        );
    }
}

package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Problem;
import lombok.Getter;

@Getter
public class ProblemInfoResponse {
    private int contestId;
    private String index;
    private String name;
    private int rating;
    private String description;
    private String inputDescription;
    private String outputDescription;
    private String timeLimit;
    private String memoryLimit;
    private String url;
    public ProblemInfoResponse(Problem problem) {
        this.contestId = problem.getContestId();
        this.index = problem.getIndex();
        this.name = problem.getName();
        this.rating = problem.getRating();
        this.description = problem.getDescription();
        this.inputDescription = problem.getInputDescription();
        this.outputDescription = problem.getOutputDescription();
        this.timeLimit = problem.getTimeLimit();
        this.memoryLimit = problem.getMemoryLimit();
        this.url = problem.getUrl();
    }
}

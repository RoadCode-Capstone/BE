package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends CreatedOnlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column
    private int contestId;

    @Column
    private String index;

    @Column
    private String name;

    @Column
    private int rating;

    @Lob
    @Column
    private String description;

    @Lob
    @Column
    private String inputDescription;

    @Lob
    @Column
    private String outputDescription;

    @Column
    private String timeLimit;

    @Column
    private String memoryLimit;

    @Lob
    @Column
    private String url;

    @OneToMany(mappedBy = "problem")
    private List<Testcase> testcases = new ArrayList<>();

    @OneToMany(mappedBy = "problem")
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "problem")
    private List<ProblemTag> problemTags = new ArrayList<>();

    @Builder
    private Problem(int contestId, String index, String name, int rating,
                   String description, String inputDescription, String outputDescription,
                   String timeLimit, String memoryLimit, String url) {
        this.contestId = contestId;
        this.index = index;
        this.name = name;
        this.rating = rating;
        this.description = description;
        this.inputDescription = inputDescription;
        this.outputDescription = outputDescription;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.url = url;
    }

    public static Problem create(int contestId, String index, String name, int rating,
                                 String description, String inputDescription, String outputDescription,
                                 String timeLimit, String memoryLimit, String url) {
        return Problem.builder()
                .contestId(contestId)
                .index(index)
                .name(name)
                .rating(rating)
                .description(description)
                .inputDescription(inputDescription)
                .outputDescription(outputDescription)
                .timeLimit(timeLimit)
                .memoryLimit(memoryLimit)
                .url(url)
                .build();
    }

}

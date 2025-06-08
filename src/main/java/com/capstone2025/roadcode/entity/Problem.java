package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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


}

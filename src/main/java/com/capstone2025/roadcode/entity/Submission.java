package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import com.capstone2025.roadcode.common.LanguageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends CreatedOnlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column
    private String sourceCode;

    @Enumerated(value = EnumType.STRING)
    private LanguageType language;

    private boolean isSuccess;

    @OneToMany(mappedBy = "submission")
    private List<Review> reviews = new ArrayList<>();

    @Builder
    private Submission(Problem problem, Member member, String sourceCode, LanguageType language, boolean isSuccess) {

        this.problem = problem;
        this.member = member;
        this.sourceCode = sourceCode;
        this.language = language;
        this.isSuccess = isSuccess;
    }

    public static Submission create(Problem problem, Member member, String sourceCode, LanguageType language, boolean isSuccess) {
        return Submission.builder()
                .problem(problem)
                .member(member)
                .sourceCode(sourceCode)
                .language(language)
                .isSuccess(isSuccess)
                .build();
    }
}

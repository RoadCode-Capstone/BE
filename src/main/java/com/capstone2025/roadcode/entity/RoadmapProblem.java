package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoadmapProblem extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_problem_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "roadmap_id")
    private Roadmap roadmap;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private int sequence; // 로드맵 내에서 해당 문제 순서를 나타냄 , 0부터 시작

    @Enumerated(value = EnumType.STRING)
    private RoadmapProblemStatus status;

    public static RoadmapProblem create(Roadmap roadmap, Problem problem, int sequence, RoadmapProblemStatus status) {
        return RoadmapProblem.builder()
                .roadmap(roadmap)
                .problem(problem)
                .sequence(sequence)
                .status(status)
                .build();
    }
}

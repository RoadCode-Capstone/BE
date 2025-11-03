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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id")
    private Roadmap roadmap;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private int order; // 로드맵 내에서 해당 문제 순서를 나타냄 , 0부터 시작

    @Enumerated(value = EnumType.STRING)
    private RoadmapProblemStatus status;

    public static RoadmapProblem create(Roadmap roadmap, Problem problem, int order, RoadmapProblemStatus status) {
        return RoadmapProblem.builder()
                .roadmap(roadmap)
                .problem(problem)
                .order(order)
                .status(status)
                .build();
    }

    // 로드맵 문제 상태를 완료로 변경
    public void complete() {
        this.status = RoadmapProblemStatus.COMPLETED;
    }

    // 로드맵 문제 상태를 진행중으로 변경
    public void startProgress() {
        this.status = RoadmapProblemStatus.IN_PROGRESS;
    }

}

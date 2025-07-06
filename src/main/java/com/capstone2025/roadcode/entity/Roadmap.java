package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.BaseEntity;
import com.capstone2025.roadcode.common.LanguageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    @Enumerated(EnumType.STRING)
    private RoadmapType type;

    @Enumerated(EnumType.STRING)
    private LanguageType language;

    private String algorithm;

    @Enumerated(EnumType.STRING)
    private RoadmapStatus status;

//    @ManyToOne
//    @JoinColumn(name = "last_solved_problem_id", nullable = true)
//    private Problem problem; // 마지막으로 푼 문제 id

    @OneToMany(mappedBy = "roadmap") // order 기준으로 오름차순 정렬(수정 필요)
    private List<RoadmapProblem> roadmapProblems = new ArrayList<>();

    @Builder
    private Roadmap(Member member, String title, RoadmapType type, LanguageType language, String algorithm, RoadmapStatus status) {
        this.member = member;
        this.title = title;
        this.type = type;
        this.language = language;
        this.algorithm = algorithm;
        this.status = status;
    }
    public static Roadmap create(Member member, String title, RoadmapType type, LanguageType language, String algorithm) {
        return Roadmap.builder()
                .member(member)
                .title(title)
                .type(type)
                .language(language)
                .algorithm(algorithm)
                .status(RoadmapStatus.IN_PROGRESS)
                .build();
    }
}

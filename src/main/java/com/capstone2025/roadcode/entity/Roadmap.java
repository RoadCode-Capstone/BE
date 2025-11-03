package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.BaseEntity;
import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private int levelTestResult; // 레벨 테스트 결과 (문제 난이도 범위와 동일)

    private int dailyGoal; // 일일 학습 목표 (문제 개수)

//    @ManyToOne
//    @JoinColumn(name = "last_solved_problem_id", nullable = true)
//    private Problem problem; // 마지막으로 푼 문제 id

    @OneToMany(mappedBy = "roadmap") // order 기준으로 오름차순 정렬(수정 필요)
    @OrderBy("order ASC")
    private List<RoadmapProblem> roadmapProblems = new ArrayList<>();

    @Builder
    private Roadmap(Member member, String title, RoadmapType type, LanguageType language, String algorithm, RoadmapStatus status, int levelTestResult, int dailyGoal) {
        this.member = member;
        this.title = title;
        this.type = type;
        this.language = language;
        this.algorithm = algorithm;
        this.status = status;
        this.levelTestResult = levelTestResult;
        this.dailyGoal = dailyGoal;
    }
    public static Roadmap create(Member member, String title, RoadmapType type, LanguageType language, String algorithm, int levelTestResult, int dailyGoal) {
        return Roadmap.builder()
                .member(member)
                .title(title)
                .type(type)
                .language(language)
                .algorithm(algorithm)
                .status(RoadmapStatus.IN_PROGRESS)
                .levelTestResult(levelTestResult)
                .dailyGoal(dailyGoal)
                .build();
    }

    // 로드맵 포기(상태 변경)
    public void giveUp() {

        if (this.status == RoadmapStatus.COMPLETED){
            throw new CustomException(ErrorCode.ROADMAP_ALREADY_COMPLETED);
        } else if (this.status == RoadmapStatus.GAVE_UP){
            throw new CustomException(ErrorCode.ROADMAP_ALREADY_GAVE_UP);
        }

        this.status = RoadmapStatus.GAVE_UP;
    }

    // 로드맵 완료(상태 변경)
    public void complete() {

        this.status = RoadmapStatus.COMPLETED;
    }

    // 현재 문제 순서 기반으로 다음 문제 찾기
    public Optional<RoadmapProblem> findNextProblem(RoadmapProblem currentProblem){
        int currentOrder = currentProblem.getOrder();

        return this.roadmapProblems.stream()
                .filter(p -> p.getOrder() > currentOrder)
                .findFirst(); // 현재 order 순서대로 정렬돼있는 상태
    }
}

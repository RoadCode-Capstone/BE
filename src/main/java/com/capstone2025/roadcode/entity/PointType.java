package com.capstone2025.roadcode.entity;

import lombok.Getter;

@Getter
public enum PointType {
    ATTENDANCE("출석", 2),
    PROBLEM_SOLVED("문제 풀이 성공", 3),
    DAILY_GOAL_COMPLETED("일일 목표 달성", 5),
    REVIEW("리뷰 작성", 1),
    ROADMAP_COMPLETED("로드맵 완성", 10);

    private final String description;
    private final int point;

    PointType(String description, int point) {
        this.description = description;
        this.point = point;
    }

}

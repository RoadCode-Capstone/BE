package com.capstone2025.roadcode.entity;

import lombok.Getter;

@Getter
public enum PointType {
    ATTENDANCE("출석", 1),
    PROBLEM_SOLVED("문제 풀이 성공", 1),
    DAILY_GOAL_COMPLETED("오늘 목표 완료", 1),
    REVIEW("리뷰 작성", 1),
    ROADMAP_COMPLETED("로드맵 완성", 1);

    private final String description;
    private final int point;

    PointType(String description, int point) {
        this.description = description;
        this.point = point;
    }

}

package com.capstone2025.roadcode.dto;


import com.capstone2025.roadcode.entity.Point;
import com.capstone2025.roadcode.entity.PointType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryByTypeResponse {

    private int totalPoint; // 해당 기간 내 포인트 총합
    private List<TypeHistory> history; // 종류별 포인트 내역 리스트

    public static PointHistoryByTypeResponse from(List<Point> points) {

        int totalPoint = points.stream().mapToInt(Point::getAmount).sum(); // 포인트 총합
        // 포인트 종류 별로 그룹화
        Map<PointType, List<Point>> grouped = points.stream()
                .collect(Collectors.groupingBy(p -> p.getPointType()));

        // 변환
        List<TypeHistory> history = grouped.entrySet().stream()
                .map(entry -> TypeHistory.from(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TypeHistory::getType))
                .toList();

        return new PointHistoryByTypeResponse(totalPoint, history);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TypeHistory {
    private PointType type; // 포인트 종류
    private int totalPoint; // 해당 종류로 얻은 포인트 총합
    private int point; // 해당 종류로 얻을 수 있는 포인트 양
    private List<LocalDate> dates; // 얻은 날짜 리스트

    static TypeHistory from(PointType type, List<Point> points) {

        int typeTotal = points.stream().mapToInt(Point::getAmount).sum();
        List<LocalDate> dates = points.stream().map(p -> p.getCreatedAt().toLocalDate()).collect(Collectors.toList());
        return new TypeHistory(type, typeTotal, type.getPoint(), dates);
    }
}

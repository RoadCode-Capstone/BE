package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Point;
import com.capstone2025.roadcode.entity.PointType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryByDateResponse {

    private int totalPoint; // 해당 기간 내 포인트 총합
    private List<DailyHistory> history; // 날짜별 포인트 내역 리스트

    public static PointHistoryByDateResponse from(List<Point> points) {

        // 전체 총합
        int totalPoint = points.stream().mapToInt(Point::getAmount).sum();

        // 날짜 기준으로 그룹화
        Map<LocalDate, List<Point>> grouped = points.stream()
                .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate()));

        // 변환
        List<DailyHistory> history = grouped.entrySet().stream()
                .map(entry -> DailyHistory.from(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyHistory::getDate))
                .toList();

        return new PointHistoryByDateResponse(totalPoint, history);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class DailyHistory {
    private LocalDate date; // 날짜
    private int totalPoint; // 해당 날짜의 포인트 총합
    private List<PointDetail> pointDetails; // 포인트 내역 상세

    static DailyHistory from(LocalDate date, List<Point> dailyPoints){

        // 해당 날짜의 포인트 총합
        int dateTotal = dailyPoints.stream().mapToInt(Point::getAmount).sum();

        // 변환
        List<PointDetail> pointDetails = dailyPoints.stream()
                .map(p -> new PointDetail(p.getPointType(), p.getAmount())).toList();

        return new DailyHistory(date, dateTotal, pointDetails);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PointDetail {

    private PointType type; // 포인트 종류
    private int point; // 포인트 양
}


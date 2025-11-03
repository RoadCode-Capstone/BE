package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Submission;
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
public class SubmissionHistoryResponse {
    private List<SubmissionHistory> history;

    public static SubmissionHistoryResponse from(List<Submission> submissions) {

        // 날짜 기준으로 그룹화
        Map<LocalDate, List<Submission>> grouped = submissions.stream()
                .collect(Collectors.groupingBy(s -> s.getCreatedAt().toLocalDate()));

        // 변환
        List<SubmissionHistory> history = grouped.entrySet().stream()
                .map(entry -> SubmissionHistory.from(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SubmissionHistory::getDate))
                .toList();

        return new SubmissionHistoryResponse(history);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SubmissionHistory{

    private LocalDate date; // 날짜
    private List<SubmissionDetail> submissionDetails; // 문제 풀이 정보 목록

    static SubmissionHistory from(LocalDate date, List<Submission> submissions){

        // 변환
        List<SubmissionDetail> submissionDetails = submissions.stream()
                .map(s -> new SubmissionDetail(
                        s.getProblem().getId(), s.getProblem().getName(), s.getId(), s.isSuccess())).toList();

        return new SubmissionHistory(date, submissionDetails);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SubmissionDetail {

    private Long problemId; // 문제 아이디
    private String problemName; // 문제 이름
    private Long submissionId; // 풀이 아이디
    private Boolean isSuccess; // 풀이 성공 여부

}



package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemListResponse;
import com.capstone2025.roadcode.dto.ProblemResponse;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public ProblemResponse getProblemInfo(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        return ProblemResponse.from(problem);
    }

    // 문제 목록 전체 조회
    public ProblemListResponse getAllProblemsWithTags() {
        List<Problem> problems = problemRepository.findAllWithTags();
        List<ProblemResponse> problemResponses = problems.stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());

        return new ProblemListResponse(problemResponses);
    }

    // 레벨 테스트 문제 조회 시 사용(ai에게서 받아온 문제 id를 모두 찾아와서 문제 목록을 반환)
    public ProblemListResponse getProblemsByIdsWithTags(List<Long> ids) {
        List<Problem> problems = problemRepository.findAllByIdInWithTags(ids);
        List<ProblemResponse> problemResponses = problems.stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());

        return new ProblemListResponse(problemResponses);
    }
}

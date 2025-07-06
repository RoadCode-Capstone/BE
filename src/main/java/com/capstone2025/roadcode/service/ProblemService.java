package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemInfoResponse;
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

    public ProblemInfoResponse getProblemInfo(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        return new ProblemInfoResponse(problem);
    }

    // 문제 목록 전체 조회
    public List<ProblemResponse> getAllProblemsWithTags() {
        List<Problem> problems = problemRepository.findAllWithTags();

        return problems.stream()
                .map(problem -> new ProblemResponse(
                        problem.getId(),
                        problem.getContestId(),
                        problem.getIndex(),
                        problem.getName(),
                        problem.getRating(),
                        problem.getDescription(),
                        problem.getInputDescription(),
                        problem.getOutputDescription(),
                        problem.getTimeLimit(),
                        problem.getMemoryLimit(),
                        problem.getUrl(),
                        //problem.getCreatedAt(),
                        problem.getProblemTags().stream()
                                .map(pt -> pt.getTag().getName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // 레벨 테스트 문제 조회 시 사용(ai에게서 받아온 문제 id를 모두 찾아와서 문제 목록을 반환)
    public List<ProblemResponse> getProblemsByIdsWithTags(List<Long> ids) {
        List<Problem> problems = problemRepository.findAllByIdInWithTags(ids);

        return problems.stream()
                .map(problem -> new ProblemResponse(
                        problem.getId(),
                        problem.getContestId(),
                        problem.getIndex(),
                        problem.getName(),
                        problem.getRating(),
                        problem.getDescription(),
                        problem.getInputDescription(),
                        problem.getOutputDescription(),
                        problem.getTimeLimit(),
                        problem.getMemoryLimit(),
                        problem.getUrl(),
                        //problem.getCreatedAt(),
                        problem.getProblemTags().stream()
                                .map(pt -> pt.getTag().getName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}

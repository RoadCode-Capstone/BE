package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemInfoResponse;
import com.capstone2025.roadcode.dto.ProblemResponseDto;
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
    public List<ProblemResponseDto> getAllProblemsWithTags() {
        List<Problem> problems = problemRepository.findAllWithTags();

        return problems.stream()
                .map(problem -> new ProblemResponseDto(
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
                        problem.getCreatedAt(),
                        problem.getProblemTags().stream()
                                .map(pt -> pt.getTag().getName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}

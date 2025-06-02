package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemInfoResponse;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public ProblemInfoResponse getProblemInfo(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        return new ProblemInfoResponse(problem);
    }
}

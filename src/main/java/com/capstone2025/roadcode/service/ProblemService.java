package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemListResponse;
import com.capstone2025.roadcode.dto.ProblemResponse;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.RoadmapType;
import com.capstone2025.roadcode.entity.Tag;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final TagService tagService;

    public ProblemResponse getProblemInfo(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        return ProblemResponse.from(problem);
    }

    // 문제 목록 전체 조회
    public List<ProblemResponse> getAllProblemsWithTags() {
        List<Problem> problems = problemRepository.findAllWithTags();
        return problems.stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());
    }

    // 레벨 테스트 문제 조회 시 사용(ai에게서 받아온 문제 id를 모두 찾아와서 문제 목록을 반환)
    public List<ProblemResponse> getProblemsByIdsWithTags(List<Long> ids) {
        List<Problem> problems = problemRepository.findAllByIdInWithTags(ids);
        return problems.stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());
    }

    public ProblemListResponse getAllProblems(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return new ProblemListResponse(getProblemsByIdsWithTags(ids));
        } else {
            return new ProblemListResponse(getAllProblemsWithTags());
        }
    }

    public List<ProblemResponse> filterByRating(List<Problem> problems, int rating) {
        return problems.stream()
                .map(ProblemResponse::from)
                .filter(p -> p.getRating() == rating)
                .collect(Collectors.toList());
    }

    public List<Problem> getProblemsByRoadmapTypeAndAlgorithm(RoadmapType type, String algorithm) {

        if (type == RoadmapType.Algorithm) {
            Tag tag = tagService.findByName(algorithm);
            return problemRepository.findAllByTagId(tag.getId());
        } else if(type == RoadmapType.Language) {
            return problemRepository.findAll();
        } else {
            throw new CustomException(ErrorCode.INVALID_ROADMAP_TYPE);
        }
    }
}

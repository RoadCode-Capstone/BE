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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // 로드맵 타입에 맞는 문제 목록 전체 가져오기
    public List<Problem> getProblemsByRoadmapTypeAndAlgorithm(RoadmapType type, String algorithm) {

        if (type == RoadmapType.Algorithm) {
            Tag tag = tagService.findByName(algorithm);
            log.info("[In ProblemService.java]");
            log.info("[tag 이름] : {}", tag);
            return problemRepository.findAllByTagId(tag.getId());
        } else if(type == RoadmapType.Language) {
            return problemRepository.findAll();
        } else {
            throw new CustomException(ErrorCode.INVALID_ROADMAP_TYPE);
        }
    }

    /**
     * 추가 문제 추천 함수(현재 로드맵 문제 아이디 목록, 현재 로드맵 알고리즘 종류, 현재 진행중인 문제 난이도, 일일 학습 목표)
     * */
    public List<Problem> getRecommendProblems(List<Long> problemIds, RoadmapType type, Long tagId, int rating, int dailyGoal) {

        // problemIds가 비어있으면 NOT IN () 쿼리 에러 발생
        if (problemIds == null || problemIds.isEmpty()) {
            log.warn("problemIds가 비어있어 추가 문제를 조회할 수 없습니다.");
            return Collections.emptyList();
        }

        // Pageable 객체 생성 (limit = dailyGoal, sort = rating ASC)
        Pageable pageable = PageRequest.of(0, dailyGoal, Sort.by(Sort.Direction.ASC, "rating"));

        // 조건에 맞는 문제 조회
        Page<Problem> problemPage;
        if(type == RoadmapType.Algorithm){

            problemPage = problemRepository.findAlgorithmProblems(
                    tagId, problemIds, rating, pageable
            );
        } else if (type == RoadmapType.Language) {

            problemPage = problemRepository.findByIdNotInAndRatingGreaterThanEqual(
                    problemIds, rating, pageable
            );

        } else {
            throw new CustomException(ErrorCode.INVALID_ROADMAP_TYPE); // 로드맵 타입 오류
        }

        if (problemPage.isEmpty()) { // 조건에 맞는 문제 없음
            log.info("조건에 맞는 문제 없음"); // 출력 변경해도 상관 없음
            throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        return problemPage.getContent();
    }

    /**
     * 개념 강화 문제 1개 추천 받기
     */
    public Optional<Problem> getConceptProblem(Long currentProblemId, List<Long> roadmapProblemIds) {

        Problem currentProblem = problemRepository.findById(currentProblemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND)); // 문제 아이디로 문제 찾기
        int currentRating = currentProblem.getRating(); // 현재 문제 난이도 가져오기

        List<Long> currentTagIds = currentProblem.getProblemTags().stream()
                .map(problemTag -> problemTag.getTag().getId())
                .collect(Collectors.toList());

        // 후보 문제 가져오기
        List<Problem> candidates = problemRepository.findReinforcementProblem(
                roadmapProblemIds, currentTagIds, currentRating
        );

        // 목록 가장 위에 있는 추천 문제 가져오기
        if(candidates.isEmpty()) {
            log.info("조건에 맞는 추천 문제 없음");
            throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        return candidates.stream().findFirst();
    }


}

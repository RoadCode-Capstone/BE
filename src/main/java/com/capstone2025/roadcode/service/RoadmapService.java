package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.entity.*;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.capstone2025.roadcode.repository.RoadmapProblemRepository;
import com.capstone2025.roadcode.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapProblemRepository roadmapProblemRepository;
    private final ProblemRepository problemRepository;
    private final OpenAIService aiService;
    private final MemberService memberService;

    // 로드맵 정보 조회(현재 진행중인 문제도 함께 줌)
    public RoadmapInfoResponse getRoadmapInfo(String email, Long roadmapId) {

        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId).
                orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));

        verifyRoadmapOwner(roadmap, member);

        RoadmapProblem roadmapProblem = roadmapProblemRepository.findByRoadmapIdAndStatus(
                roadmapId, RoadmapProblemStatus.IN_PROGRESS)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        RoadmapProblemResponse currentProblem = new RoadmapProblemResponse(
                roadmapProblem.getId(), roadmapProblem.getProblem().getId(), roadmapProblem.getSequence(), roadmapProblem.getStatus());

        return new RoadmapInfoResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getType(),
                roadmap.getLanguage().toString(),
                roadmap.getAlgorithm(),
                currentProblem,
                roadmap.getLevelTestResult(),
                roadmap.getDailyGoal());
    }

    // 로드맵 문제 목록 조회
    public RoadmapProblemListResponse getRoadmapProblems(String email, Long roadmapId) {

        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId).
                orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));

        verifyRoadmapOwner(roadmap, member);

        List<RoadmapProblem> roadmapProblems = roadmapProblemRepository.findByRoadmapId(roadmapId);

        List<RoadmapProblemResponse> roadmapProblemResponses = roadmapProblems.stream()
                .map(RoadmapProblemResponse::from)
                .collect(Collectors.toList());
        return new RoadmapProblemListResponse(roadmapProblemResponses);
    }

    // 문제 list 로드맵 db에 저장
    @Transactional // 문제 못찾는 경우, roadmap save도 취소
    public ReturnIdResponse createRoadmap(RoadmapCreateRequest request, String email) {

        RoadmapType type =  RoadmapType.fromString(request.getType());
        LanguageType language = LanguageType.fromString(request.getLanguage());
        String algorithm = request.getAlgorithm();
        int dailyGoal = request.getDailyGoal();
        int levelTestResult = request.getLevelTestResult();

        List<Long> problemIds = aiService.createRoadmap(
                type,
                algorithm,
                request.getDailyGoal(),
                request.getLevelTestResult()
        );

        Member member = memberService.findByEmail(email);

        String roadmapName = createRoadmapName(type, language, algorithm);
        Roadmap roadmap = Roadmap.create(
                member, roadmapName, type, language, algorithm, dailyGoal, levelTestResult);


        roadmapRepository.save(roadmap);

        for(int i = 0; i < problemIds.size(); i++) {
            Problem problem = problemRepository.findById(problemIds.get(i))
                    .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

            // 첫번째 문제는 "IN_PROGRESS"
            RoadmapProblemStatus status = (i == 0)
                    ? RoadmapProblemStatus.IN_PROGRESS
                    : RoadmapProblemStatus.NOT_STARTED;

            RoadmapProblem roadmapProblem = RoadmapProblem.create(roadmap, problem, i, status);
            roadmapProblemRepository.save(roadmapProblem);
        }

        return new ReturnIdResponse(roadmap.getId());
    }

    private String createRoadmapName(RoadmapType type, LanguageType language, String algorithm){
        return type.toString()
                + (algorithm != null ? " " + algorithm : "")
                + " " + language.toString() + " 로드맵";
    }

    // 로드맵 목록 조회
    public RoadmapListResponse getRoadmaps(String email, List<RoadmapStatus> statusList) {
        Member member = memberService.findByEmail(email);
        List<RoadmapResponse> roadmaps = roadmapRepository.findRoadmapsByMemberAndStatus(member, statusList)
                .stream()
                .map(RoadmapResponse::from)
                .collect(Collectors.toList());

        return new RoadmapListResponse(roadmaps);
    }

    // 로드맵 삭제 (시연)
    @Transactional
    public void deleteRoadmap(Long roadmapId, String email) {

        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));
        verifyRoadmapOwner(roadmap, member);
        roadmapRepository.delete(roadmap);
    }

    // 로드맵 포기
    @Transactional
    public void giveUpRoadmap(Long roadmapId, String email) {

        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));
        verifyRoadmapOwner(roadmap, member);
        roadmap.giveUp();
    }

    // 로드맵 사용자 검증
    public void verifyRoadmapOwner(Roadmap roadmap, Member member) {
        if (roadmap.getMember().getId() != member.getId()) {
            throw new CustomException(ErrorCode.ROADMAP_ACCESS_DENIED);
        }
    }
}

package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.RoadmapInfoResponse;
import com.capstone2025.roadcode.dto.RoadmapProblemResponse;
import com.capstone2025.roadcode.dto.RoadmapRequest;
import com.capstone2025.roadcode.dto.RoadmapResponse;
import com.capstone2025.roadcode.entity.*;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.capstone2025.roadcode.repository.RoadmapProblemRepository;
import com.capstone2025.roadcode.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public RoadmapInfoResponse getRoadmapInfo(Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId).
                orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));

        RoadmapProblem roadmapProblem = roadmapProblemRepository.findByRoadmapIdAndStatus(
                roadmapId, RoadmapProblemStatus.IN_PROGRESS)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        RoadmapProblemResponse currentProblem = new RoadmapProblemResponse(
                roadmapProblem.getId(), roadmapProblem.getProblem().getId(), roadmapProblem.getSequence(), roadmapProblem.getStatus());

        return new RoadmapInfoResponse(
                roadmap.getId(), roadmap.getTitle(), roadmap.getType(), roadmap.getCategory(), currentProblem);
    }

    // 로드맵 문제 목록 조회
    public List<RoadmapProblemResponse> getRoadmapProblems(Long roadmapId) {

        List<RoadmapProblem> roadmapProblems = roadmapProblemRepository.findByRoadmapId(roadmapId);

        List<RoadmapProblemResponse> roadmapProblemResponses = roadmapProblems.stream()
                .map(rp -> new RoadmapProblemResponse(
                        rp.getId(),
                        rp.getProblem().getId(),
                        rp.getSequence(),
                        rp.getStatus()
                ))
                .collect(Collectors.toList());
        return roadmapProblemResponses;
    }

    // 문제 list 로드맵 db에 저장
    @Transactional // 문제 못찾는 경우, roadmap save도 취소
    public void createRoadmap(RoadmapRequest request, String email) {

        String type =  request.getType();
        String category = request.getCategory();

        List<Long> problemIds = aiService.createRoadmap(
                type,
                category,
                request.getDailyGoal(),
                request.getLevelTestResult()
        );

        // 서버 올리기 전 삭제
        System.out.println(problemIds);

        problemIds.clear();
        problemIds.add(1L);
        problemIds.add(2L);

        Member member = memberService.findByEmail(email);

        Roadmap roadmap = Roadmap.create(
                member, createRoadmapName(type, category), RoadmapType.valueOf(type), category);


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
    }

    private String createRoadmapName(String type, String category){
        return type + " " + category + " 로드맵";
    }

    public List<RoadmapResponse> getRoadmaps(String email) {
        Member member = memberService.findByEmail(email);
        List<RoadmapResponse> roadmaps = roadmapRepository.findByMember(member).stream()
                .map(rm -> new RoadmapResponse(rm.getId(), rm.getTitle(), rm.getType(), rm.getCategory()))
                .collect(Collectors.toList());

        return roadmaps;
    }
}

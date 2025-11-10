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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapProblemRepository roadmapProblemRepository;
    private final ProblemRepository problemRepository;
    private final OpenAIService aiService;
    private final MemberService memberService;
    private final ProblemService problemService;
    private final TagService tagService;

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

            // 중간에 문제 추가될 가능성을 고려하여 order은 10, 20, 30 이렇게 저장
            // 중간에 문제가 추가될 경우 +1하여 저장
            RoadmapProblem roadmapProblem = RoadmapProblem.create(roadmap, problem, i*10, status);
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

        if(statusList == null || statusList.isEmpty()) {
            statusList = null;
        }

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

    // 로드맵에서 다음 문제로 넘어가기
    @Transactional
    public void completeProblemAndAdvance(Long currentRoadmapProblemId) {

        RoadmapProblem currentProblem = roadmapProblemRepository.findById(
                        currentRoadmapProblemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        Roadmap roadmap = currentProblem.getRoadmap();

        // 1. 현재 문제 완료 상태로 변경
        currentProblem.complete();

        // 2-1. 다음 문제가 있을 경우, 진행 중으로 변경
        Optional<RoadmapProblem> nextProblemOpt = roadmap.findNextProblem(currentProblem);
        if(nextProblemOpt.isPresent()) {
            RoadmapProblem nextProblem = nextProblemOpt.get();
            nextProblem.startProgress();
        } else { // 2-2. 다음 문제가 없을 경우, 로드맵 상태를 완료로 변경
            roadmap.complete();
        }



    }

    // 로드맵에 개념 강화 문제 추가
    @Transactional
    public void addConceptProblem(Long roadmapId, String email, AddConceptProblemRequest request) {
        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));
        verifyRoadmapOwner(roadmap, member);

        Long currentProblemId = request.getCurrentProblemId(); // 현재 문제 가져오기
        List<Long> roadmapProblemIds = roadmap.getRoadmapProblems().stream()
                        .map(rp -> rp.getProblem().getId())
                                .toList();
        Optional<Problem> conceptProblem = problemService.getConceptProblem(currentProblemId, roadmapProblemIds);

        if(conceptProblem.isPresent()) {
            // 현재 진행중인 문제 sequence 가져오기
            int currentSequence = getCurrentProblemSequence(roadmapId);

            // 현재 진행중인 문제는 NOT_STARTED로 바꾸기
            resetCurrentProblemToNotStart(roadmapId);

            // 로드맵에 문제 추가
            RoadmapProblem roadmapProblem = RoadmapProblem.create(
                    roadmap, conceptProblem.get(), currentSequence-1, RoadmapProblemStatus.IN_PROGRESS);
            roadmapProblemRepository.save(roadmapProblem);
        } else {
            // 없을 때 오류 날리기
        }
    }

    // 로드맵에 추천 문제 추가
    @Transactional
    public void addRecommendProblems(Long roadmapId, String email) {
        Member member = memberService.findByEmail(email);
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND));
        verifyRoadmapOwner(roadmap, member);

        List<Long> roadmapProblemIds = roadmap.getRoadmapProblems().stream()
                .map(rp -> rp.getProblem().getId())
                .toList();
        RoadmapType type = roadmap.getType();

        Long tagId = tagService.findByName(roadmap.getAlgorithm()).getId();
        int rating = roadmap.getLevelTestResult();
        int dailyGoal = roadmap.getDailyGoal();

        // 추천 문제 가져오기
        List<Problem> recommendProblems = problemService.getRecommendProblems(
                roadmapProblemIds, type, tagId, rating, dailyGoal
        );

        // 로드맵 마지막 문제 뒤에 새로운 문제 추가하기
        List<RoadmapProblem> sortedProblems = roadmap.getRoadmapProblems();
        int lastIndex = sortedProblems.get(sortedProblems.size() - 1).getSequence();

        int index = 0; // recommendProblems의 인덱스
        for(Problem problem: recommendProblems) {

            // 첫번째 문제는 "IN_PROGRESS"
            RoadmapProblemStatus status = (index == 0)
                    ? RoadmapProblemStatus.IN_PROGRESS
                    : RoadmapProblemStatus.NOT_STARTED;

            RoadmapProblem roadmapProblem = RoadmapProblem.create(roadmap, problem, lastIndex + index, status);
            roadmapProblemRepository.save(roadmapProblem);

            index++;
        }
    }

    /**
     * 로드맵에서 현재 진행 중인 문제의 순서(sequence)를 반환합니다.
     *
     * @param roadmapId 로드맵 ID
     * @return 현재 진행 중인 문제의 sequence
     */
    public int getCurrentProblemSequence(Long roadmapId) {

        RoadmapProblem currentProblem = findInProgressProblem(roadmapId);
        return currentProblem.getSequence();
    }

    /**
     * 로드맵에서 현재 진행 중인 문제를 NOT_START 상태로 변경합니다.
     *
     * @param roadmapId 로드맵 ID
     */
    @Transactional
    public void resetCurrentProblemToNotStart(Long roadmapId) {

        RoadmapProblem currentProblem = findInProgressProblem(roadmapId);

        // 엔티티의 상태 변경 (JPA가 변경 감지 - Dirty Checking)
        currentProblem.init();
    }


    /**
     * 로드맵 ID로 현재 "진행 중(IN_PROGRESS)"인 RoadmapProblem 엔티티를 찾습니다.
     *
     * @param roadmapId 로드맵 ID
     * @return RoadmapProblem 엔티티
     */
    private RoadmapProblem findInProgressProblem(Long roadmapId) {
        return roadmapProblemRepository.findByRoadmapIdAndStatus(roadmapId, RoadmapProblemStatus.IN_PROGRESS)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
    }

}

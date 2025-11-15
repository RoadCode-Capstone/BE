package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.entity.*;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.capstone2025.roadcode.repository.SubmissionRepository;
import com.capstone2025.roadcode.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final TestcaseRepository testcaseRepository;
    private final ProblemRepository problemRepository;
    private final MemberService memberService;
    private final SubmissionRepository submissionRepository;
    private final RoadmapService roadmapService;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher; // 문제 풀이 성공 시 사용

    @Value("${spring.code.save-dir}") // 로컬 환경 path(서버로 변경하면 바꿔야함)
    private String codeSaveDir;
    @Value("${spring.code.save-file}")
    private String codeFileName;

    // 로드맵 풀이 제출 결과
    @Transactional
    public SubmitSolutionResponse submitRoadmapSolution(String email, Long problemId, SubmitSolutionRequest request) {

        Member member = memberService.findByEmail(email);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        RoadmapInfoResponse roadmap = roadmapService.getRoadmapInfo(email, request.getRoadmapId());

        LanguageType language = LanguageType.fromString(request.getLanguage());
        String code = request.getSourceCode();

        List<TestcaseResult> testcaseResults = submitSolution(problemId, language, code); // 풀이 제출
        boolean allPassed = isAllPassed(testcaseResults); // 테스트 케이스 전부 통과했는지 확인

        // 5. db에 저장
        Submission submission = Submission.create(problem, member, code, language, allPassed);
        submissionRepository.save(submission);

        // 6. 문제 풀이에 성공한 경우 로드맵 다음 문제로 수정
        if(allPassed){
            //roadmapService.completeProblemAndAdvance(member, request.getRoadmapProblemId());
            //pointService.giveSolutionPoint(member); // 포인트 지급
            SubmissionSuccessEvent event = new SubmissionSuccessEvent(
                    submission.getId(),
                    member.getId(),
                    request.getRoadmapProblemId()
            );
            eventPublisher.publishEvent(event);
            log.info("문제 풀이 성공");
        }

        // 7. 전체 결과를 응답에 추가
        int dailyGoal = roadmap.getDailyGoal(); // 로드맵 일일 목표
        LocalDateTime startDate = LocalDate.now().atStartOfDay(); // 오늘 00:00
        LocalDateTime endDate = startDate.plusDays(1); // 내일 00:00
        int dailyCompleted = submissionRepository.countByMemberIdAndCreatedAtBetweenAndIsSuccessTrue(
                member.getId(), startDate, endDate
                );

        float achievementRate = getDailyAchievementRate(member, dailyGoal,  dailyCompleted); // 달성률

        return new SubmitSolutionResponse(allPassed, testcaseResults, dailyGoal, dailyCompleted, achievementRate);
    }


    // 풀이 제출 결과 반환
    public List<TestcaseResult> submitSolution(Long problemId, LanguageType language, String code){

        List<Testcase> testcases = testcaseRepository.findByProblemId(problemId);
        if (testcases.size() == 0) {
            throw new CustomException(ErrorCode.TESTCASE_NOT_FOUND_FOR_PROBLEM);
        }

        // 1. 코드 파일 생성(임시 저장) & 코드 파일을 실행하기 위한 정보를 ctx에 저장
        DockerExecutionContext ctx = prepareCodeFile(language, code);

        List<TestcaseResult> testcaseResults = new ArrayList<>();

        // 테스트케이스만큼 반복문 돌리기
        for (Testcase tc : testcases) {

            String input = tc.getInput();
            String output = tc.getOutput();
            // 2. 도커 명령어 생성
            ProcessBuilder pb = buildDockerCommand(ctx);

            // 3. 도커 명령어 실행 및 결과 반환
            TestcaseResult testcaseResult = executeWithInput(pb, input, output);
            testcaseResults.add(testcaseResult);
        }

        // 4. 임시 파일 삭제
        deleteCodeDirectory(ctx.getCodeDir());

        return testcaseResults;
    }

    /**
     * 사용자의 일일 학습 달성률을 (퍼센트, 0-100)로 반환합니다.
     * @return 달성률 (float)
     */
    public float getDailyAchievementRate(Member member, int dailyGoal, int dailyCompleted) {

        if(dailyGoal <= dailyCompleted) { // 일일 학습 목표 달성
            pointService.giveDailyGoalPoint(member); // 포인트 지급
            return 100f;
        }

        float achievementRate;

        // 목표가 0일 경우 (0으로 나누기 방지)
        if (dailyGoal == 0) {
            // 목표가 0인데 완료했든 안했든, 목표는 '달성'한 것으로 봅니다.
            achievementRate = 100.0f;
        } else {
            // (float) 형변환으로 정확한 소수점 계산
            float rawRate = ((float) dailyCompleted / dailyGoal) * 100;

            // 100%를 넘지 않도록 상한선 적용
            achievementRate = Math.min(rawRate, 100.0f);
        }

        return achievementRate;
    }

    // 코드 파일 생성 및 도커 실행 dto 생성
    private DockerExecutionContext prepareCodeFile(LanguageType language, String sourceCode) {
        String uuid = UUID.randomUUID().toString();
        String codeDir = codeSaveDir + "/" + uuid;

        try {
            Files.createDirectories(Paths.get(codeDir));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_WRITE_FAILED);
        }

        LanguageType languageType = language;
        String codeFilePath = codeDir + "/" + codeFileName + languageType.getExtension();

        try {
            Files.writeString(Paths.get(codeFilePath), sourceCode);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_WRITE_FAILED);
        }

        DockerExecutionContext ctx = new DockerExecutionContext(languageType, codeDir);
        return ctx;
    }

    // 도커 명령어 생성
    private ProcessBuilder buildDockerCommand(DockerExecutionContext ctx) {
        List<String> command = Arrays.asList(
                "docker", "run", "--rm", "-i",
                "-v", ctx.getCodeDir() + ":/app",
                "-w", "/app", ctx.getLanguageType().getImage(),
                "sh", "-c", ctx.getLanguageType().getCommand()
        );

        log.info("[Docker command] : \n" + String.join(" ", command));
        return new ProcessBuilder(command);
    }

    // 테스트케이스 실행 코드
    private TestcaseResult executeWithInput(ProcessBuilder pb, String input, String expectedOutput) {
        try {
            log.info("[프로세스 시작]");
            log.info("입력값:\n" + input);
            log.info("기대 출력값:\n" + expectedOutput);

            Process process = pb.start();

            try(
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            ) {
// 입력 전달
                writer.write(input);
                writer.newLine();
                writer.flush();

                // 정상 출력 읽기
                StringBuilder outputBuilder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    outputBuilder.append(line).append("\n");
                }
                String actualOutput = outputBuilder.toString().trim();
                log.info("실제 출력값:\n" + actualOutput);

                // 표준 에러 읽기
                StringBuilder stderrBuilder = new StringBuilder();
                String errLine;
                while ((errLine = errReader.readLine()) != null) {
                    stderrBuilder.append(errLine).append("\n");
                }

                process.waitFor();

                boolean passed = expectedOutput.trim().equals(actualOutput != null ? actualOutput.trim() : "");

                if (passed) {
                    log.info("테스트 통과");
                    return new TestcaseResult(true, null);
                } else {
                    log.info("테스트 실패");

                    String errorMsg = stderrBuilder.toString().trim();
                    log.info("표준 에러 출력:\n" + errorMsg);

                    if (!errorMsg.isEmpty()) {
                        return new TestcaseResult(false, errorMsg);
                    } else {
                        return new TestcaseResult(false, "결과값이 다릅니다.");
                    }
                }
            }

        } catch (Exception e) {
            log.info("예외 발생: " + e.getMessage());
            return new TestcaseResult(false, "예외 발생: " + e.getMessage());
        }
    }

    // 코드 파일(상위 디렉토리) 삭제
    public void deleteCodeDirectory(String codeDir) {
        try {
            Path dirPath = Paths.get(codeDir);
            if (Files.exists(dirPath)) {
                // 디렉토리 안의 파일들 먼저 삭제
                Files.walk(dirPath)
                        .sorted(Comparator.reverseOrder()) // 파일 먼저, 디렉토리 나중
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 레벨테스트 제출
    @Transactional
    public LevelTestResultResponse submitLevelTestSolution(String email, LevelTestSubmissionsRequest request) {

        List<Boolean> result = new ArrayList<>();

        for(SubmitLevelTestRequest solution : request.getSubmissions()){

            Long problemId = solution.getProblemId();
            LanguageType language = LanguageType.fromString(solution.getLanguage());
            String code = solution.getSourceCode();

            List<TestcaseResult> testcaseResults = submitSolution(problemId, language, code); // 풀이 제출
            boolean allPassed = isAllPassed(testcaseResults); // 테스트 케이스 전부 통과했는지 확인

            if(allPassed) {
                result.add(true);
            } else {
                result.add(false);
            }
        }

        long count = result.stream()
                .filter(b -> b == true)
                .count();

        return new LevelTestResultResponse(request.getSubmissions().size(), result, (int)count);
    }

    // 테스트 케이스 통과 여부 판단
    private boolean isAllPassed(List<TestcaseResult> testcaseResults) {
        for(TestcaseResult testcaseResult : testcaseResults) {
            if(!testcaseResult.isPassed()) {
                return false;
            }
        }
        return true;
    }

    // 특정 문제에 대한 다른 사람 풀이 목록 가져오기
    public OtherMemberSubmissionListResponse getOtherSuccessfulSubmissions(String email, Long problemId) {

        Member member = memberService.findByEmail(email);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        // 사용자가 해당 문제를 풀었는지 검사
        validateSolvedProblem(member.getId(), problemId);

        List<Submission> submissions = submissionRepository.findByProblemIdAndIsSuccessTrue(problemId);

        List<OtherMemberSubmissionResponse> submissionsResponse = submissions.stream()
                .map(OtherMemberSubmissionResponse::from)
                .collect(Collectors.toList());

        return new OtherMemberSubmissionListResponse(submissionsResponse);
    }

    // 사용자가 해당 문제를 풀었는지 검사
    public void validateSolvedProblem(Long memberId, Long problemId) {

        boolean isSolved = submissionRepository.existsByMemberIdAndProblemIdAndIsSuccessTrue(memberId, problemId);
        if(!isSolved){
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
    }

    // 특정 풀이 아이디로 조회
    public Submission findById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBMISSION_NOT_FOUND));
    }

    // 특정 풀이 조회 후 반환
    public SubmissionResponse getSubmission(String email, Long submissionId) {
        Submission submission = findById(submissionId);
        return SubmissionResponse.from(submission);
    }

    // 사용자 문제 풀이 목록 조회
    public SubmissionHistoryResponse getSubmissions(String email, String start, String end, Boolean isSuccess) {
        Member member = memberService.findByEmail(email); // 로그인 사용자 가져오기
        Long memberId = member.getId();

        try{
            LocalDateTime startDate = LocalDate.parse(start).atStartOfDay(); // 시작 날짜 00:00
            LocalDateTime endDate = LocalDate.parse(end).plusDays(1).atStartOfDay(); // 끝 날짜 다음날 00:00

            List<Submission> submissions = submissionRepository.findSubmissions(memberId, isSuccess, startDate, endDate);

            return SubmissionHistoryResponse.from(submissions);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }

}

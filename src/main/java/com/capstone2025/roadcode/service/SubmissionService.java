package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.Submission;
import com.capstone2025.roadcode.entity.Testcase;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.MemberRepository;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.capstone2025.roadcode.repository.SubmissionRepository;
import com.capstone2025.roadcode.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${spring.code.save-dir}") // 로컬 환경 path(서버로 변경하면 바꿔야함)
    private String codeSaveDir;
    @Value("${spring.code.save-file}")
    private String codeFileName;

    // 풀이 제출
    @Transactional
    public SubmitSolutionResponse submitSolution(String email, Long problemId, SubmitSolutionRequest request){

        Member member = memberService.findByEmail(email);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        LanguageType language = LanguageType.fromString(request.getLanguage());
        String code = request.getSourceCode();

        List<Testcase> testcases = testcaseRepository.findByProblemId(problemId);
        if (testcases.size() == 0) {
            throw new CustomException(ErrorCode.TESTCASE_NOT_FOUND_FOR_PROBLEM);
        }

        // 1. 코드 파일 생성(임시 저장) & 코드 파일을 실행하기 위한 정보를 ctx에 저장
        DockerExecutionContext ctx = prepareCodeFile(language, code);

        List<TestcaseResult> testcaseResults = new ArrayList<>();
        boolean allPassed = true;

        // 테스트케이스만큼 반복문 돌리기
        for (Testcase tc : testcases) {

            String input = tc.getInput();
            String output = tc.getOutput();
            // 2. 도커 명령어 생성
            ProcessBuilder pb = buildDockerCommand(ctx);

            // 3. 도커 명령어 실행 및 결과 반환
            TestcaseResult testcaseResult = executeWithInput(pb, input, output);
            testcaseResults.add(testcaseResult);

            if(!testcaseResult.isPassed()){
                allPassed = false;
            }
        }

        // 4. 임시 파일 삭제
        deleteCodeDirectory(ctx.getCodeDir());

        // 5. db에 저장
        Submission submission = Submission.create(problem, member, code, language, allPassed);
        submissionRepository.save(submission);

        // 6. 문제 풀이에 성공한 경우 로드맵 다음 문제로 수정
        if(request.getRoadmapId()!= null && allPassed){
            roadmapService.completeProblemAndAdvance(request.getRoadmapProblemId());
        }

        // 7. 전체 결과를 응답에 추가
        return new SubmitSolutionResponse(allPassed, testcaseResults);
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
    public LevelTestResultResponse submitLevelTest(String email, LevelTestSubmissionsRequest request) {

        List<Boolean> result = new ArrayList<>();

        for(SubmitLevelTestRequest solution : request.getSubmissions()){

            SubmitSolutionRequest submitSolutionRequest = new SubmitSolutionRequest(
                    solution.getLanguage(), solution.getSourceCode());
            SubmitSolutionResponse response = submitSolution(email, solution.getProblemId(), submitSolutionRequest);

            if(response.isAllPassed()) {
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

    public Submission findById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBMISSION_NOT_FOUND));
    }

    public SubmissionResponse getSubmission(String email, Long submissionId) {
        Submission submission = findById(submissionId);
        return SubmissionResponse.from(submission);
    }
}

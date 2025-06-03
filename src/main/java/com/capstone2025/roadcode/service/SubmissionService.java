package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.dto.DockerExecutionContext;
import com.capstone2025.roadcode.dto.SubmitSolutionRequest;
import com.capstone2025.roadcode.dto.SubmitSolutionResponse;
import com.capstone2025.roadcode.dto.TestcaseResult;
import com.capstone2025.roadcode.entity.Testcase;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final TestcaseRepository testcaseRepository;

    @Value("${spring.code.save-dir}") //로컬 환경 path(서버로 변경하면 바꿔야함)
    private String codeSaveDir;
    @Value("${spring.code.save-file}")
    private String codeFileName;

    // 풀이 제출
    public SubmitSolutionResponse submitSolution(String email, Long problemId, SubmitSolutionRequest request){
        String language = request.getLanguage();
        String code = request.getSourceCode();

        // db 저장 코드 추가(마지막에)

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

        // 5. 전체 결과를 응답에 추가
        return new SubmitSolutionResponse(allPassed, testcaseResults);
    }

    // 코드 파일 생성 및 도커 실행 dto 생성
    private DockerExecutionContext prepareCodeFile(String language, String sourceCode) {
        String uuid = UUID.randomUUID().toString();
        String codeDir = codeSaveDir + "/" + uuid;

        try {
            Files.createDirectories(Paths.get(codeDir));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_WRITE_FAILED);
        }

        LanguageType languageType = LanguageType.fromString(language);
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
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm", "-i",
                "-v", ctx.getCodeDir() + ":/app",
                "-w", "/app", ctx.getLanguageType().getImage(),
                "sh", "-c", ctx.getLanguageType().getCommand()
        );

        return pb;
    }

    // 테스트케이스 실행 코드
    private TestcaseResult executeWithInput(ProcessBuilder pb, String input, String expectedOutput) {
        try {
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

                // 표준 에러 읽기
                StringBuilder stderrBuilder = new StringBuilder();
                String errLine;
                while ((errLine = errReader.readLine()) != null) {
                    stderrBuilder.append(errLine).append("\n");
                }

                process.waitFor();

                boolean passed = expectedOutput.trim().equals(actualOutput != null ? actualOutput.trim() : "");

                if (passed) {
                    return new TestcaseResult(true, null);
                } else {
                    String errorMsg = stderrBuilder.toString().trim();
                    if (!errorMsg.isEmpty()) {
                        return new TestcaseResult(false, errorMsg);
                    } else {
                        return new TestcaseResult(false, "결과값이 다릅니다.");
                    }
                }
            }

        } catch (Exception e) {
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
}

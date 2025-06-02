package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.dto.DockerExecutionContext;
import com.capstone2025.roadcode.dto.SubmitSolutionRequest;
import com.capstone2025.roadcode.dto.SubmitSolutionResponse;
import com.capstone2025.roadcode.dto.TestcaseResult;
import com.capstone2025.roadcode.entity.Testcase;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.capstone2025.roadcode.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionServiceMySelf {

    private final TestcaseRepository testcaseRepository;

    @Value("${code.local-save-dir}")
    private String saveDir;

    @Value("${code.save-file}")
    private String saveFileName;

    public SubmitSolutionResponse submitSolution(String email, Long problemId, SubmitSolutionRequest request) {

        String language = request.getLanguage();
        String sourceCode = request.getSourceCode();

        // 1. 테스트 케이스 찾기
        List<Testcase> testcases = testcaseRepository.findByProblemId(problemId);

        if(testcases.size() == 0) {
            throw new CustomException(ErrorCode.TESTCASE_NOT_FOUND_FOR_PROBLEM);
        }

        DockerExecutionContext ctx = prepareFile(language, sourceCode);

        ProcessBuilder pb = buildCommand(ctx);

        List<TestcaseResult> testcaseResults = new ArrayList<>();
        boolean allPassed = true;

        for(Testcase tc : testcases) {
            String input = tc.getInput();
            String output = tc.getOutput();

            TestcaseResult testcaseResult = executeCommand(pb, input, output);
            testcaseResults.add(testcaseResult);

            if(!testcaseResult.isPassed()){
                allPassed = false;
            }
        }

        deleteCodeFile(ctx.getCodeDir());

        return new SubmitSolutionResponse(allPassed, testcaseResults);

    }

    private DockerExecutionContext prepareFile(String language, String sourceCode) {
        // 1. uuid 디렉터리 생성
        String uuid = UUID.randomUUID().toString();
        String codeDir = saveDir + "/" + uuid;
        try{
            Files.createDirectory(Paths.get(codeDir));
        } catch(Exception e){
            throw new CustomException(ErrorCode.FILE_WRITE_FAILED);
        }

        // 2. 파일 경로에 소스코드 담아서 저장
        LanguageType languageType = LanguageType.fromString(language);
        String filePath = codeDir + "/" + saveFileName + languageType.getExtension();

        try{
            Files.writeString(Paths.get(filePath), sourceCode);
        } catch(Exception e){
            throw new CustomException(ErrorCode.FILE_WRITE_FAILED);
        }

        // 3. DockerExecutionContext 리턴
        return new DockerExecutionContext(languageType, codeDir);
    }

    private ProcessBuilder buildCommand(DockerExecutionContext ctx) {
        return new ProcessBuilder(
                "docker", "run", "--rm", "-i",
                "-v", ctx.getCodeDir() + ":/app",
                "-w", "/app", ctx.getLanguageType().getImage(),
                "sh", "-c", ctx.getLanguageType().getCommand()
        );
    }

    private TestcaseResult executeCommand(ProcessBuilder pb, String input, String expectedOutput) {
        try{
            Process process = pb.start();
            // 입력 쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(input);
            writer.newLine();
            writer.flush();
            writer.close();

            // 표준 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                outputBuilder.append(line).append("\n");
            }
            String actualOutput = outputBuilder.toString().trim();

            // 에러 출력 읽기
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder stderrBuilder = new StringBuilder();
            String errLine;
            while((errLine = stderrReader.readLine())!= null){
                stderrBuilder.append(errLine).append("\n");
            }
            String stderr = stderrBuilder.toString().trim();

            process.waitFor();

            boolean passed = expectedOutput.trim().equals(actualOutput != null ? actualOutput : "");

            if(passed){
                return new TestcaseResult(passed, null);
            } else {
                if (stderr.isEmpty()){
                    return new TestcaseResult(passed, "결과값이 다릅니다.");
                } else {
                    return new TestcaseResult(passed, stderr);
                }
            }



        } catch(Exception e) {
            return new TestcaseResult(false, "예외 발생 : "+ e.getMessage()); // 수정해야함
        }
    }

    private void deleteCodeFile(String codeDir) {

        Path dirPath = Paths.get(codeDir);
        try {
            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch(Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

}

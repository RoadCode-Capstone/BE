package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemResponse;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.RoadmapType;
import com.capstone2025.roadcode.entity.Tag;
import com.capstone2025.roadcode.repository.ProblemRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.*;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {
    @Value("${openai.api.key}")
    private String apiKey;

    private final ProblemService problemService;

//    public static void main(String[] args) {
////        List<Problem> problems = getProblems();   // TEST
////        List<Integer> roadmapProblemIds = createRoadmap("language", "", 1, 1500);   // TEST
//        List<Integer> levelTestProblemIds = createLevelTest("language", "");  // TEST
//    }


    /* AI 응답 생성 함수 */
    public String createAIResponse(String rule, String prompt) {
        OkHttpClient client = new OkHttpClient();

        // 메시지 구성
        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", rule);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        // 요청 JSON
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4o-mini");
        requestBody.add("messages", messages);

        // 로그 출력
        log.info("[AI 요청 생성]");
        log.info("Prompt: {}", prompt);

        // HTTP 요청
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        // 응답 처리
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("AI 호출 실패: {}", response);
                throw new RuntimeException("AI 호출 실패: " + response);
            }

            String responseBody = response.body().string();

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = json
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            log.info("AI 응답 결과: {}", content.trim());
            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 처리 중 오류 발생", e);
        }
    }

    /* 학습 로드맵 생성 함수(학습 유형, 알고리즘, 일일 학습 목표, 레벨테스트 결과) */
    public List<Long> createRoadmap(RoadmapType type, String algorithm, int dailyGoal, int levelTestResult) {
        /*
        레벨 테스트 결과를 기준으로 -100 ~ +500
        총 문제 수 : 일일 학습 목표 * 14 (2주)
        난이도별 개수 : (-100, 2), (0, 2), (+100, 4), (+200, 3), (+300, 2), (+400, 1)
        특정 난이도의 후보 문제 중 랜덤으로 결정
        선택한 문제 아이디 리스트를 리턴
         */

        List<Problem> problems = problemService.getProblemsByRoadmapTypeAndAlgorithm(type, algorithm);

        // 난이도별 개수 저장
        Map<Integer, Integer> ratingProblemCnt = new LinkedHashMap<>();
        ratingProblemCnt.put(-100, 2);
        ratingProblemCnt.put(0, 2);
        ratingProblemCnt.put(100, 4);
        ratingProblemCnt.put(200, 3);
        ratingProblemCnt.put(300, 2);
        ratingProblemCnt.put(400, 1);

        // 선택한 문제 아이디 리스트
        List<Long> problemIds = new ArrayList<>();

        // 레벨테스트 결과 최솟값 900으로 설정
        if (levelTestResult < 900) {
            levelTestResult = 900;
        }

        // 특정 난이도의 후보 문제 중 랜덤으로 결정하여 문제 아이디 저장
        for (Map.Entry<Integer, Integer> entry : ratingProblemCnt.entrySet()) {
            int diff = entry.getKey();  // 난이도 차이
            int cnt = entry.getValue() * dailyGoal; // 선택할 문제 수 (일일 학습 목표 * 난이도별 문제 수)
            int targetRating = levelTestResult + diff;  // 후보 난이도

            // 후보 난이도에 해당하는 문제 목록을 후보로 저장
            List<ProblemResponse> targetProblems = problemService.filterByRating(problems, targetRating);
            String problemText = convertProblemsToText(targetProblems);

            // 프롬프트 작성 (문제 목록 중에서 cnt개 만큼 골라서 고른 문제의 id만 보내줘)
            String rule = "You're a problem recommender. The user gives a list of problems, one per line, in the format: 'id, tag1 tag2 ...'. Choose N relevant problems and return only their ids, separated by spaces. No explanation.";
            String prompt = "N=" + cnt + "\n" + problemText;

            // AI 응답 생성
            String response = createAIResponse(rule, prompt);
//            System.out.println(response); // TEST

            // 선택한 문제 아이디를 리스트에 추가
            for (String id : response.trim().split(" ")) {
                if (!id.isEmpty()) {
                    problemIds.add(Long.parseLong(id));
                }
            }
        }

        problemIds.add(0, 2195l); // Hello World 문제 추가(시연 후 해당 코드 삭제)

        // 선택한 문제 아이디 리스트 리턴
        return problemIds;
    }

    /* 레벨테스트 생성 함수(학습 유형, 알고리즘) */
    public List<Long> createLevelTest(RoadmapType type, String algorithm) {
        /*
        총 문제 수 : 5
        각 문제 난이도 : 800, 1000, 1300, 1500, 1800
        특정 난이도의 후보 문제 중 랜덤으로 결정
        선택한 문제 아이디 리스트를 리턴
         */

        List<Problem> problems = problemService.getProblemsByRoadmapTypeAndAlgorithm(type, algorithm);

        // 각 문제 난이도 저장
        List<Integer> problemRatings = new ArrayList<>(Arrays.asList(800, 1000, 1300, 1500, 1800));

        // 선택한 문제 아이디 리스트
        List<Long> problemIds = new ArrayList<>();

        // 후보 문제 개수 확인
        int targetProblemCnt = 0;

        // 특정 난이도의 후보 문제 중 랜덤으로 결정하여 문제 아이디 저장
        for (int targetRating : problemRatings) {
            // 후보 난이도에 해당하는 문제 목록을 후보로 저장
            List<ProblemResponse> targetProblems = problemService.filterByRating(problems, targetRating);
            targetProblemCnt += targetProblems.size();
            String problemText = convertProblemsToText(targetProblems);

            // 프롬프트 작성 (문제 목록 중에서 cnt개 만큼 골라서 고른 문제의 id만 보내줘)
            String rule = "You're a problem recommender. The user gives a list of problems, one per line, in the format: 'id, tag1 tag2 ...'. Choose 1 problem and return only its id. No explanation.";
            String prompt = problemText;

            // AI 응답 생성
            String response = createAIResponse(rule, prompt);

            // 선택한 문제 아이디를 리스트에 추가
            for (String id : response.trim().split(" ")) {
                if (!id.isEmpty()) {
                    problemIds.add(Long.parseLong(id));
                }
            }
        }

        log.info("[해당 알고리즘(태그)을 가진 문제 개수] : {}", problems.size());
        log.info("[후보 난이도에 해당하는 문제 개수] : {}", targetProblemCnt);

        // 선택한 문제 아이디 리스트 리턴
        return problemIds;
    }

    private String convertProblemsToText(List<ProblemResponse> problems) {
        StringBuilder stringBuilder = new StringBuilder();
        Gson gson = new Gson(); // 한 번만 생성

        // id, tags만 보내면 약 10원. description도 같이 보내면 약 200원. (2000문제 기준)
        // 후보 문제의 아이디와 태그를 텍스트화
        for (ProblemResponse problem : problems) {
            stringBuilder.append(problem.getProblemId())
                    .append(", ")
                    .append(gson.toJson(problem.getTags()))
                    .append("\n");
        }

        return stringBuilder.toString();
    }
}
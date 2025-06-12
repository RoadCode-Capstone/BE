package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.ProblemResponseDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
public class OpenAIService {
    private static final String OPENAI_API_KEY = getApiKey();

//    public static void main(String[] args) {
////        List<Problem> problems = getProblems();   // TEST
////        List<Integer> roadmapProblemIds = createRoadmap("language", "", 1, 1500);   // TEST
//        List<Integer> levelTestProblemIds = createLevelTest("language", "");  // TEST
//    }

    /* config.properties에서 OpenAI API key 조회 함수 */
    public static String getApiKey() {
        try (InputStream in = OpenAIService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("openai.api.key");
        } catch (Exception e) {
            throw new RuntimeException("API 키 로딩 실패", e);
        }
    }

    /* 전체 문제 목록 조회 함수 */
    public List<ProblemResponseDto> getProblems() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://3.35.192.94:8080/api/v1/problems")
                .addHeader("Authorization", "Bearer fixed-test-token")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("응답 실패: " + response.code());
                return emptyList();
            }

            String responseBody = response.body().string();

            // JSON 파싱
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ProblemResponseDto>>() {}.getType();
            List<ProblemResponseDto> problemList = gson.fromJson(responseBody, listType);

            return problemList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

        // HTTP 요청
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        // 응답 처리
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("AI 호출 실패: " + response);
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = json
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 처리 중 오류 발생", e);
        }
    }

    /* 학습 로드맵 생성 함수(학습 유형, 알고리즘, 일일 학습 목표, 레벨테스트 결과) */
    public List<Integer> createRoadmap(String type, String algorithm, int dailyGoal, int levelTestResult) {
        /*
        레벨 테스트 결과를 기준으로 -100 ~ +500
        총 문제 수 : 일일 학습 목표 * 14 (2주)
        난이도별 개수 : (-100, 2), (0, 2), (+100, 4), (+200, 3), (+300, 2), (+400, 1)
        특정 난이도의 후보 문제 중 랜덤으로 결정
        선택한 문제 아이디 리스트를 리턴
         */

        // 전체 문제 목록을 후보로 저장
        List<ProblemResponseDto> problems = getProblems();

        // 학습 유형이 알고리즘이면 해당하는 문제 목록을 후보로 저장
        if (type.equals("algorithm")) {
            problems = filterByTag(problems, algorithm);
        }

        // 난이도별 개수 저장
        Map<Integer, Integer> ratingProblemCnt = new LinkedHashMap<>();
        ratingProblemCnt.put(-100, 2);
        ratingProblemCnt.put(0, 2);
        ratingProblemCnt.put(100, 4);
        ratingProblemCnt.put(200, 3);
        ratingProblemCnt.put(300, 2);
        ratingProblemCnt.put(400, 1);

        // 선택한 문제 아이디 리스트
        List<Integer> problemIds = new ArrayList<>();

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
            List<ProblemResponseDto> targetProblems = filterByRating(problems, targetRating);

            // id, tags만 보내면 약 10원. description도 같이 보내면 약 200원. (2000문제 기준)
            // 후보 문제의 아이디와 태그를 텍스트화
            StringBuilder stringBuilder = new StringBuilder();
            for (ProblemResponseDto problem : targetProblems) {
                stringBuilder.append(problem.getId())
                        .append(", ")
                        .append(new Gson().toJson(problem.getTags()))
                        .append("\n");
            }
            String problemText = stringBuilder.toString();

            // 프롬프트 작성 (문제 목록 중에서 cnt개 만큼 골라서 고른 문제의 id만 보내줘)
            String rule = "You're a problem recommender. The user gives a list of problems, one per line, in the format: 'id, tag1 tag2 ...'. Choose N relevant problems and return only their ids, separated by spaces. No explanation.";
            String prompt = "N=" + cnt + "\n" + problemText;

            // AI 응답 생성
            String response = createAIResponse(rule, prompt);
//            System.out.println(response); // TEST

            // 선택한 문제 아이디를 리스트에 추가
            for (String id : response.trim().split(" ")) {
                if (!id.isEmpty()) {
                    problemIds.add(Integer.parseInt(id));
                }
            }
        }
//        System.out.println("problemIds: " + problemIds);  // TEST

        // 선택한 문제 아이디 리스트 리턴
        return problemIds;
    }

    /* 레벨테스트 생성 함수(학습 유형, 알고리즘) */
    public List<Integer> createLevelTest(String type, String algorithm) {
        /*
        총 문제 수 : 5
        각 문제 난이도 : 800, 1000, 1300, 1500, 1800
        특정 난이도의 후보 문제 중 랜덤으로 결정
        선택한 문제 아이디 리스트를 리턴
         */

        // 전체 문제 목록을 후보로 저장
        List<ProblemResponseDto> problems = getProblems();

        // 학습 유형이 알고리즘이면 해당하는 문제 목록을 후보로 저장
        if (type.equals("algorithm")) {
            problems = filterByTag(problems, algorithm);
        }

        // 각 문제 난이도 저장
        List<Integer> problemRatings = new ArrayList<>(Arrays.asList(800, 1000, 1300, 1500, 1800));

        // 선택한 문제 아이디 리스트
        List<Integer> problemIds = new ArrayList<>();

        // 특정 난이도의 후보 문제 중 랜덤으로 결정하여 문제 아이디 저장
        for (int targetRating : problemRatings) {
            // 후보 난이도에 해당한느 문제 목록을 후보로 저장
            List<ProblemResponseDto> targetProblems = filterByRating(problems, targetRating);

            // 후보 문제의 아이디와 태그를 텍스트화
            StringBuilder stringBuilder = new StringBuilder();
            for (ProblemResponseDto problem : targetProblems) {
                stringBuilder.append(problem.getId())
                        .append(", ")
                        .append(new Gson().toJson(problem.getTags()))
                        .append("\n");
            }
            String problemText = stringBuilder.toString();

            // 프롬프트 작성 (문제 목록 중에서 cnt개 만큼 골라서 고른 문제의 id만 보내줘)
            String rule = "You're a problem recommender. The user gives a list of problems, one per line, in the format: 'id, tag1 tag2 ...'. Choose 1 problem and return only its id. No explanation.";
            String prompt = problemText;

            // AI 응답 생성
            String response = createAIResponse(rule, prompt);
            System.out.println(response); // TEST

            // 선택한 문제 아이디를 리스트에 추가
            for (String id : response.trim().split(" ")) {
                if (!id.isEmpty()) {
                    problemIds.add(Integer.parseInt(id));
                }
            }
        }
        System.out.println("problemIds: " + problemIds);  // TEST

        // 선택한 문제 아이디 리스트 리턴
        return problemIds;
    }

    /* 특정 태그를 가지는 문제 목록 조회 함수 */
    public List<ProblemResponseDto> filterByTag(List<ProblemResponseDto> problems, String targetTag) {
        return problems.stream()
                .filter(p -> p.getTags() != null && p.getTags().contains(targetTag))
                .collect(Collectors.toList());
    }

    /* 특정 난이도를 가지는 문제 목록 조회 함수 */
    public List<ProblemResponseDto> filterByRating(List<ProblemResponseDto> problems, int targetRating) {
        return problems.stream()
                .filter(p -> p.getRating() == targetRating)
                .collect(Collectors.toList());
    }
}
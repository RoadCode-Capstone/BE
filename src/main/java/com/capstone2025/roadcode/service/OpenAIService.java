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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        // 응답 시간 늘림
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

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

    /* {변겅} 학습 로드맵 생성 함수(학습 유형, 알고리즘, 일일 학습 목표, 레벨테스트 결과) */
    public List<Long> createRoadmap(RoadmapType type, String algorithm, int dailyGoal, int levelTestResult) {
        /*
          레벨 테스트 결과를 기준으로 -100 ~ +500
          총 문제 수 : 일일 학습 목표 * 14 (2주)
          난이도별 개수 : (-100, 2), (0, 2), (+100, 4), (+200, 3), (+300, 2), (+400, 1)
          방향 규칙: 음수 → 낮은 쪽 근접 / 양수 → 높은 쪽 근접 / 0 → 정확히, 부족하면 높은 쪽 → 그래도 부족하면 낮은 쪽
        */

        List<Problem> problems = problemService.getProblemsByRoadmapTypeAndAlgorithm(type, algorithm);

        // rating -> 문제 리스트 (정렬 접근을 위해 TreeMap)
        NavigableMap<Integer, List<Problem>> byRating = new TreeMap<>();
        for (Problem p : problems) {
            byRating.computeIfAbsent(p.getRating(), k -> new ArrayList<>()).add(p);
        }

        // 레벨테스트 결과 최솟값 900
        if (levelTestResult < 900) levelTestResult = 900;

        // 난이도별 "배분 계수" (dailyGoal은 아래에서 한 번만 곱함)
        Map<Integer, Integer> ratingProblemCnt = new LinkedHashMap<>();
        ratingProblemCnt.put(-100, 2);
        ratingProblemCnt.put(0,    2);
        ratingProblemCnt.put(100,  4);
        ratingProblemCnt.put(200,  3);
        ratingProblemCnt.put(300,  2);
        ratingProblemCnt.put(400,  1);

        Set<Long> usedIds = new HashSet<>();

        // 선택한 문제 id 리스트(로드맵 결과)
        List<Long> problemIds = new ArrayList<>();

        final int POOL_MULTIPLIER = 3; // 필요한 개수의 몇 배까지 풀을 모을지

        for (Map.Entry<Integer, Integer> e : ratingProblemCnt.entrySet()) {
            int diff = e.getKey();
            int cnt  = e.getValue() * dailyGoal;
            int targetRating = levelTestResult + diff;

            // 1) 방향 우선 수집
            List<Problem> pool;
            if (diff < 0) {
                pool = collectLowerOrEqual(byRating, targetRating, cnt * POOL_MULTIPLIER, usedIds);   // 낮은 쪽 우선
                // 2) 부족/없으면 반대방향 보충(높은 쪽)
                if (pool.size() < cnt) {
                    List<Problem> opp = collectHigherOrEqual(byRating, targetRating + 1, (cnt - pool.size()) * POOL_MULTIPLIER, usedIds);
                    addUniqueById(pool, opp);
                }
            } else if (diff > 0) {
                pool = collectHigherOrEqual(byRating, targetRating, cnt * POOL_MULTIPLIER, usedIds);   // 높은 쪽 우선
                // 2) 부족/없으면 반대방향 보충(낮은 쪽)
                if (pool.size() < cnt) {
                    List<Problem> opp = collectLowerOrEqual(byRating, targetRating - 1, (cnt - pool.size()) * POOL_MULTIPLIER, usedIds);
                    addUniqueById(pool, opp);
                }
            } else { // diff == 0
                pool = new ArrayList<>();
                // 정확히 target 먼저
                List<Problem> exact = byRating.getOrDefault(targetRating, Collections.emptyList());
                for (Problem p : exact) if (!usedIds.contains(p.getId())) pool.add(p);
                // 부족하면 높은 쪽 보충
                if (pool.size() < cnt) {
                    List<Problem> hi = collectHigherOrEqual(byRating, targetRating + 1, (cnt - pool.size()) * POOL_MULTIPLIER, usedIds);
                    addUniqueById(pool, hi);
                }
                // 그래도 부족하면 낮은 쪽 보충
                if (pool.size() < cnt) {
                    List<Problem> lo = collectLowerOrEqual(byRating, targetRating - 1, (cnt - pool.size()) * POOL_MULTIPLIER, usedIds);
                    addUniqueById(pool, lo);
                }
            }

            if (pool.isEmpty()) continue;

            // AI로 N개 선택(검증), 부족분 랜덤 보충
            List<Long> picked = pickIdsByAIOrRandom(pool, cnt, usedIds);

            for (long id : picked) {
                problemIds.add(id);
                usedIds.add(id);
            }
        }

        // 3) 난이도 오름차순 정렬 (동률 tie-breaker 없음)
        Map<Long, Integer> idToRating = new HashMap<>();
        for (Problem p : problems) idToRating.put(p.getId(), p.getRating());
        problemIds.sort(Comparator.comparingInt(id -> idToRating.getOrDefault(id, Integer.MAX_VALUE)));

        problemIds.add(0, 2195l); // Hello World 문제 추가(시연 후 해당 코드 삭제)

        // 선택한 문제 id 리스트 리턴
        return problemIds;
    }

    /* {변경} 레벨테스트 생성 함수(학습 유형, 알고리즘) */
    public List<Long> createLevelTest(RoadmapType type, String algorithm) {

        // 1) 전체 후보
        List<Problem> problems = problemService.getProblemsByRoadmapTypeAndAlgorithm(type, algorithm);

        // ★중요: JSON이 problemId라면 아래 Problem.id 매핑 확인 (예: @SerializedName("problemId") public int id;)
        Map<Integer, List<Problem>> byRating = new HashMap<>();
        for (Problem p : problems) {
            byRating.computeIfAbsent(p.getRating(), k -> new ArrayList<>()).add(p);
        }

        // 선택한 문제 아이디 리스트
        List<Long> problemIds = new ArrayList<>();

        Set<Long> usedIds = new HashSet<>();
        Random random = new Random();
        List<Integer> targetRatings = Arrays.asList(800, 1000, 1300, 1500, 1800);

        for (int target : targetRatings) {
            // 2) 정확 난이도 후보
            List<Problem> candidates = byRating.getOrDefault(target, Collections.emptyList());

            // 3) 아직 안 뽑힌 것만
            List<Problem> filtered = new ArrayList<>();
            for (Problem p : candidates) {
                if (!usedIds.contains(p.getId())) filtered.add(p);
            }

            // 4) 정확 난이도에서 못 뽑으면, 근접 난이도 중 '아직 안 뽑힌' 후보 재탐색
            if (filtered.isEmpty()) {
                filtered = nearestWithAvailable(byRating, target, usedIds);
            }
            if (filtered.isEmpty()) {
                // 정말 후보가 없으면 스킵 (정책에 따라 전체 랜덤 등으로 바꿀 수 있음)
                continue;
            }

            // 5) 선택 (AI 사용 시 검증 포함, 여기선 랜덤)
            Problem pick = filtered.get(random.nextInt(filtered.size()));
            problemIds.add(pick.getId());
            usedIds.add(pick.getId());
        }
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

    // {변경} 낮은 쪽(<= target)에서 가까운 난이도부터 need개 정도 풀을 모아온다(usedIds 제외)
    private static List<Problem> collectLowerOrEqual(
            NavigableMap<Integer, List<Problem>> byRating,
            int target, int need, Set<Long> usedIds
    ) {
        List<Problem> out = new ArrayList<>();
        Integer key = byRating.floorKey(target);   // target 이하에서 시작
        while (key != null && out.size() < need) {
            for (Problem p : byRating.get(key)) {
                if (!usedIds.contains(p.getId())) {
                    out.add(p);
                    if (out.size() >= need) break;
                }
            }
            key = byRating.lowerKey(key);          // 더 낮은 난이도로 이동
        }
        return out;
    }

    // {변경} 높은 쪽(>= target)에서 가까운 난이도부터 need개 정도 풀을 모아온다(usedIds 제외)
    private List<Problem> collectHigherOrEqual(
            NavigableMap<Integer, List<Problem>> byRating,
            int target, int need, Set<Long> usedIds
    ) {
        List<Problem> out = new ArrayList<>();
        Integer key = byRating.ceilingKey(target); // target 이상에서 시작
        while (key != null && out.size() < need) {
            for (Problem p : byRating.get(key)) {
                if (!usedIds.contains(p.getId())) {
                    out.add(p);
                    if (out.size() >= need) break;
                }
            }
            key = byRating.higherKey(key);         // 더 높은 난이도로 이동
        }
        return out;
    }

    // {변경} dest에 src를 id 기준으로 중복 없이 합침
    private void addUniqueById(List<Problem> dest, List<Problem> src) {
        if (src == null || src.isEmpty()) return;
        Set<Long> seen = new HashSet<>();
        for (Problem p : dest) seen.add(p.getId());
        for (Problem p : src) {
            if (!seen.contains(p.getId())) {
                dest.add(p);
                seen.add(p.getId());
            }
        }
    }

    // {변경} AI로 cnt개 고르고, 부족하면 랜덤 보충(usedIds/중복 체크)
    private List<Long> pickIdsByAIOrRandom(List<Problem> pool, int cnt, Set<Long> usedIds) {
        List<Long> chosen = new ArrayList<>();
        if (pool.isEmpty() || cnt <= 0) return chosen;

        // AI 프롬프트 준비

        // entity -> dto 리스트로 변경
        List<ProblemResponse> problemResponses = pool.stream()
                                            .map(ProblemResponse::from)
                                            .collect(Collectors.toList());

        String problemText = convertProblemsToText(problemResponses);
        String rule =
                "You're a problem recommender. The user gives a list of problems, one per line, " +
                        "in the format: 'id, tag1 tag2 ...'. Choose N relevant problems and return only " +
                        "their ids, separated by spaces. No explanation.";
        String prompt = "N=" + cnt + "\n" + problemText;

        // 풀 id 집합
        Set<Long> poolIds = new HashSet<>();
        for (Problem p : pool) poolIds.add(p.getId());

        // AI 선택 시도
        try {
            String resp = createAIResponse(rule, prompt);
            String[] toks = resp.trim().split("\\D+");
            for (String t : toks) {
                if (t.isEmpty()) continue;
                long id = Long.parseLong(t);
                if (poolIds.contains(id) && !usedIds.contains(id) && !chosen.contains(id)) {
                    chosen.add(id);
                    if (chosen.size() == cnt) break;
                }
            }
        } catch (Exception ignored) { /* 실패 시 랜덤 보충으로 진행 */ }

        // 부족분 랜덤 보충
        if (chosen.size() < cnt) {
            List<Long> remain = new ArrayList<>();
            for (Problem p : pool) {
                if (!usedIds.contains(p.getId()) && !chosen.contains(p.getId())) remain.add(p.getId());
            }
            Collections.shuffle(remain, new Random());
            for (long id : remain) {
                chosen.add(id);
                if (chosen.size() == cnt) break;
            }
        }
        return chosen;
    }

    /* {변경} target과 가장 가까운 난이도 순서대로, 아직 안 뽑힌 문제 후보를 찾아 반환 */
    private List<Problem> nearestWithAvailable(
            Map<Integer, List<Problem>> byRating,
            int target,
            Set<Long> usedIds
    ) {
        List<Integer> ratings = new ArrayList<>(byRating.keySet());
        ratings.sort((a, b) -> {
            int da = Math.abs(a - target);
            int db = Math.abs(b - target);
            if (da != db) return Integer.compare(da, db);
            // 동률이면 높은 난이도 우선
            return Integer.compare(b, a);
        });

        for (int r : ratings) {
            List<Problem> bucket = byRating.get(r);
            if (bucket == null || bucket.isEmpty()) continue;

            List<Problem> filtered = new ArrayList<>();
            for (Problem p : bucket) {
                if (!usedIds.contains(p.getId())) filtered.add(p);
            }
            if (!filtered.isEmpty()) return filtered;
        }
        return Collections.emptyList();
    }

    /* {변경} AI 코드 리뷰 함수(문제 정보, 작성한 코드) */
    public String getAICodeReview(Problem problem, String code) {
        String rule = "Review code briefly. Focus on complexity, correctness, edge cases, tests, and improvements. Write in Korean, casual tone, no numbering, just short sentences.";
        String prompt =
                "Lang=" + "JAVA" +
                        "\nProblem=" + problem.getDescription() +
                        "\nInput=" + problem.getInputDescription() +
                        "\nOutput=" + problem.getOutputDescription() +
                        "\nTime=" + problem.getTimeLimit() +
                        "\nMem=" + problem.getMemoryLimit() +
                        "\nCode=" + code;

        String response = createAIResponse(rule, prompt);
        log.info("<<AI 코드 리뷰>>");
        log.info(response);

        return response;
    }

    // new 코드


    /*  리뷰 유효성 검사 함수(작성한 리뷰, 문제 내용, 코드 내용) */
    public Boolean isValidReview(String review, String description, String code) {
        String rule =
                "You are a strict code review validator.\n" +
                        "Decide if the given review is relevant to the provided problem and code.\n" +
                        "Rules:\n" +
                        "- If the review discusses code, logic, performance, readability, structure, or naming → valid: true.\n" +
                        "- Ignore factual errors (e.g., wrong complexity still true).\n" +
                        "- If off-topic, nonsense, or unrelated (e.g., jokes, chatting) → valid: false with short Korean reason.\n" +
                        "Output JSON only:\n" +
                        "{\"valid\": true}\n" +
                        "or\n" +
                        "{\"valid\": false, \"reason\": \"<이유>\"}";


        String prompt =
                "Problem=\n" + description +
                        "\nCode=\n" + code +
                        "\nReview=\n" + review;

        String response = createAIResponse(rule, prompt).toLowerCase();
        log.info("<<리뷰 유효성 검사(AI)>>");
        log.info(response);

        // 결과 파싱 (true / false)
        if (response.contains("true")) {
            return true;
        } else if (response.contains("false")) {
            return false;
        } else {
            System.err.println("AI 응답이 예기치 않은 형식입니다: " + response);
            return false;
        }
    }

    /* 답글 유효성 검사 함수(답글을 달 리뷰, 작성한 답글) */
    public Boolean isValidComment(String review, String comment) {
        String rule =
                "You are a strict reply validator.\n" +
                        "Given a review and its reply, decide if the reply is relevant and appropriate as a response.\n" +
                        "Rules:\n" +
                        "- If the reply responds to, asks about, or disagrees with the review’s content (code, logic, performance, readability, etc.) → valid: true.\n" +
                        "- Ignore correctness.\n" +
                        "- If off-topic, rude, meaningless, or chatting → valid: false with short Korean reason.\n" +
                        "Output only JSON:\n" +
                        "{\"valid\": true}\n" +
                        "or\n" +
                        "{\"valid\": false, \"reason\": \"<이유>\"}";

        String prompt =
                "Review=\n" + review +
                        "\nComment=\n" + comment;

        String response = createAIResponse(rule, prompt).toLowerCase();
        log.info("<<답글 유효성 검사(AI)>>");
        log.info(response);

        // 결과 파싱 (true / false)
        if (response.contains("true")) {
            return true;
        } else if (response.contains("false")) {
            return false;
        } else {
            System.err.println("AI 응답이 예기치 않은 형식입니다: " + response);
            return false;
        }
    }

//    /* 추가 문제 추천 함수(현재 로드맵 문제 아이디 목록, 현재 로드맵 알고리즘 종류, 현재 진행중인 문제 난이도, 일일 학습 목표) */
//    public List<Problem> getAdditionalProblems(List<Long> problemIds, RoadmapType type, Long tagId, int rating, int dailyGoal) {
////        /*
////        언어 유형(algorithm == null)이면 problemIds에 없는 문제이면서 rating과 같거나 높은 난이도를 가지는(같은 문제 우선) 문제를 dailyGoal개 뽑아 리턴
////        알고리즘 유형(algorithm != null)이면 problemIds에 없는 문제이면서 algorithm을 tag로 가지고 rating과 같거나 높은 난이도를 가지는(같은 문제 우선) 문제를 dailyGoal개 뽑아 리턴
////
////        TODO: 문제 추가할 때 현재 문제에서 뒷 순서로 추가해야하고, 난이도 순서대로 정렬 해주세요!!!
////         */
////
////        // 전체 문제 목록 조회
////        List<Problem> problems = problemService.getProblemsByRoadmapTypeAndAlgorithm(type, algorithm);
////
////        Stream<Problem> stream = problems.stream()
////                .filter(p -> !problemIds.contains(p.getId()))    // 현재 로드맵에 없는 문제
////                .filter(p -> p.getRating() >= rating);           // 현재 문제와 난이도가 같거나 높음
////
////        List<Problem> candidates = stream
////                .sorted(Comparator
////                        .comparingInt((Problem p) -> Math.abs(p.getRating() - rating))   // 같은 난이도 우선
////                        .thenComparingInt(p -> p.rating))                           // 그다음 난이도 오름차순
////                .limit(dailyGoal)                                                   // 일일 학습 목표 개수만큼 뽑음
////                .collect(Collectors.toList());
//
//        // 조건에 맞는 문제 조회
//        List<Problem> problems =  new ArrayList<>();
//        if(type == RoadmapType.Algorithm){
//            problems = problemService.getAlgorithmAdditionalProblems(tagId, problemIds, rating, dailyGoal);
//        } else if (type == RoadmapType.Language) {
//            problems = problemService.getLanguageAdditionalProblems(problemIds, rating, dailyGoal);
//        } else {
//            return null; // 오류?
//        }
//
//        if (problems.isEmpty()) { // 조건에 맞는 문제 없음
//            log.info("조건에 맞는 문제 없음"); // 출력 변경해도 상관 없음
//            // 응답 메시지 담아서 보내야하나;;..
//            return null;
//        }
//
//        return problems;
//    }

//    /* 개념 강화 문제 추천 함수(현재 로드맵 문제 아이디 목록, 현재 문제 태그 목록, 현재 문제 난이도) */
//    public static Problem recommendProblem(List<Integer> problemIds, int curProblemId) {
//        List<Problem> problems = getProblems(); // 전체 문제 목록 조회
//
//        List<Problem> fullMatch = problems.stream()
//                .filter(p -> p.tags != null && p.tags.containsAll(tags))    // 모든 태그 일치
//                .filter(p -> !problemIds.contains(p.id))                    // 현재 로드맵에 없는 문제
//                .filter(p -> p.rating < rating)                             // 현재 문제보다 난이도 낮음
//                .collect(Collectors.toList());
//
//        List<Problem> partialMatch = problems.stream()
//                .filter(p -> p.tags != null && p.tags.stream().anyMatch(tags::contains))    // 태그 하나라도 일치
//                .filter(p -> !problemIds.contains(p.id))                                    // 현재 로드맵에 없는 문제
//                .filter(p -> p.rating < rating)                                             // 현재 문제보다 난이도 낮음
//                .collect(Collectors.toList());
//
//        List<Problem> candidates = !fullMatch.isEmpty() ? fullMatch : partialMatch; // 모든 태그 일치가 있으면 우선, 없으면 부분 일치
//
//        if (candidates.isEmpty()) { // 조건에 맞는 문제 없음
//            System.out.println("조건에 맞는 문제 없음"); // 출력 변경해도 상관 없음
//            return null;
//        }
//
//        candidates.sort((a, b) -> Integer.compare(b.rating, a.rating)); // 난이도 기준 내림차순 정렬
//
//        Problem recommended = candidates.get(0);    // 현재보다 조금 낮은 문제를 개념 강화 문제로 추천
//
//        // TEST 결과 출력 (없어도 됨)
//        System.out.println("추천 문제: " + recommended.id + "\n" + recommended.name + " (" + recommended.rating + ", " + recommended.tags + ")");
//
//        return recommended;
//    }

}
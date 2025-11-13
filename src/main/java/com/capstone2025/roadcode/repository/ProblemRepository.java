package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // 모든 문제를 태그와 함께 반환
    @Query("SELECT DISTINCT p FROM Problem p " +
            "LEFT JOIN FETCH p.problemTags pt " +
            "LEFT JOIN FETCH pt.tag")
    List<Problem> findAllWithTags();

    // AI에게서 받아온 문제 ID에 해당하는 문제를 반환
    @Query("SELECT DISTINCT p FROM Problem p " +
            "LEFT JOIN FETCH p.problemTags pt " +
            "LEFT JOIN FETCH pt.tag " +
            "WHERE p.id IN :ids")
    List<Problem> findAllByIdInWithTags(@Param("ids") List<Long> ids);

    // 태그에 해당하는 문제를 모두 반환
    @Query("SELECT DISTINCT p FROM Problem p " +
            "JOIN p.problemTags pt " +
            "JOIN pt.tag t " +
            "WHERE t.id = :tagId")
    List<Problem> findAllByTagId(@Param("tagId") Long tagId);

    // 추천 문제 받기(로드맵 종류 = 알고리즘)
    /**
     * 1. 로드맵 타입이 "알고리즘"인 경우 (JOIN 필요)
     * tagId, problemIds, rating 조건을 모두 만족하는 문제를 rating 오름차순으로 조회
     */
    @Query("SELECT DISTINCT p FROM Problem p " +
            "JOIN p.problemTags pt " + // Problem 엔티티 내부의 List<ProblemTag> 필드명
            "JOIN pt.tag t " +         // ProblemTag 엔티티 내부의 Tag 필드명
            "WHERE t.id = :tagId " +
            "AND p.id NOT IN :problemIds " +
            "AND p.rating >= :rating")
    Page<Problem> findAlgorithmProblems(
            @Param("tagId") Long tagId,
            @Param("problemIds") Collection<Long> problemIds,
            @Param("rating") int rating,
            Pageable pageable // 여기에 Sort(rating ASC)와 Limit(dailyGoal) 정보가 담김
    );

    // 추천 문제 받기(로드맵 종류 = 언어)
    /**
     * 2. 로드맵 타입이 "언어"인 경우 (JOIN 불필요)
     * problemIds, rating 조건을 모두 만족하는 문제를 rating 오름차순으로 조회
     */
    Page<Problem> findByIdNotInAndRatingGreaterThanEqual(
            Collection<Long> problemIds,
            int rating,
            Pageable pageable // 여기에 Sort(rating ASC)와 Limit(dailyGoal) 정보가 담김
    );

    /**
     * [개념강화 문제 추천 쿼리]
     * 1. 로드맵 문제 제외 (NOT IN roadmapProblemIds)
     * 2. 현재 문제보다 낮은 난이도 (< currentRating)
     * 3. 현재 문제의 태그와 일치하는 태그 (IN currentTagIds)
     * 4. [정렬1] 일치하는 태그 개수 내림차순 (COUNT... DESC)
     * 5. [정렬2] 난이도 내림차순 (p.rating DESC)
     */
    @Query("SELECT p FROM Problem p " +
            "JOIN p.problemTags pt " +
            "WHERE p.id NOT IN :roadmapProblemIds " +
            "  AND p.rating <= :currentRating " +
            "  AND pt.tag.id IN :currentTagIds " +
            "GROUP BY p " +
            "ORDER BY COUNT(pt.tag.id) DESC, p.rating DESC")
    List<Problem> findReinforcementProblem( // 메서드 이름 단순화
                                            @Param("roadmapProblemIds") List<Long> roadmapProblemIds,
                                            @Param("currentTagIds") List<Long> currentTagIds,
                                            @Param("currentRating") int currentRating
    );


}

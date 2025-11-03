package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // 사용자가 해당 문제를 풀었는지 검사
    boolean existsByMemberIdAndProblemIdAndIsSuccessTrue(Long memberId, Long problemId);

    // 성공한 문제풀이 모두 반환
    List<Submission> findByProblemIdAndIsSuccessTrue(Long problemId);

    /**
     * 사용자가 푼 문제풀이를 조건에 따라 검색
     *
     * @param memberId (필수) 사용자 ID
     * @param isSuccess (선택) 성공/실패 여부. (true: 성공, false: 실패, null: 모두)
     * @param startDate (필수) 검색 시작 기간
     * @param endDate (필수) 검색 종료 기간
     * @return Submission 리스트
     */
    @Query("SELECT s FROM Submission s " +
            "WHERE s.member.id = :memberId " +
            "AND (s.createdAt BETWEEN :startDate AND :endDate) " +
            "AND (:isSuccess IS NULL OR s.isSuccess = :isSuccess)")
    List<Submission> findSubmissions(
            Long memberId,
            Boolean isSuccess,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

}

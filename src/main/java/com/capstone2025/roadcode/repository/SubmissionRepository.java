package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // 사용자가 해당 문제를 풀었는지 검사
    boolean existsByMemberIdAndProblemIdAndIsSuccessTrue(Long memberId, Long problemId);

    // 성공한 문제풀이 모두 반환
    List<Submission> findByProblemIdAndIsSuccessTrue(Long problemId);
}

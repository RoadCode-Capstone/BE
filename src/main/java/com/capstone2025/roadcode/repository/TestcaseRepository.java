package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Testcase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestcaseRepository extends JpaRepository<Testcase, Long> {
    List<Testcase> findByProblemId(Long problemId);
}

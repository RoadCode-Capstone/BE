package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query("SELECT DISTINCT p FROM Problem p " +
            "LEFT JOIN FETCH p.problemTags pt " +
            "LEFT JOIN FETCH pt.tag")
    List<Problem> findAllWithTags();
}

package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}

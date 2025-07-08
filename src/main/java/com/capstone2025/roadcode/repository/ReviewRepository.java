package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT DISTINCT r FROM Review r " +
            "LEFT JOIN FETCH r.comments " +
            "WHERE r.submission.id = :submissionId"
    )
    List<Review> findAllBySubmissionIdWithComments(@Param("submissionId") Long submissionId);
}

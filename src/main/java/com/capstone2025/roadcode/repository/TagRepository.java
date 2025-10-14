package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름 정렬 로직
    String TAG_ORDER_BY_CLAUSE = "ORDER BY " +
            "CASE WHEN CONVERT(name USING utf8mb4) REGEXP '^[가-힣]' THEN 0 ELSE 1 END, " +
            "CONVERT(name USING utf8mb4) COLLATE utf8mb4_general_ci";

    // 지정된 숫자 이상의 문제 수를 가진 태그의 이름을 조회
    @Query(value = "SELECT t.name FROM tag t " +
            "JOIN problem_tag pt ON t.tag_id = pt.tag_id " +
            "GROUP BY t.tag_id " +
            "HAVING COUNT(pt.problem_id) >= :problemCountThreshold " +
            TAG_ORDER_BY_CLAUSE, nativeQuery = true)
    List<String> findTagNamesWithProblemCount(int problemCountThreshold);

    Optional<Tag> findByName(String name);
}

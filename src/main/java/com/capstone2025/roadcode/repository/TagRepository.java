package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query(value = "SELECT name FROM tag ORDER BY " +
            "CASE WHEN CONVERT(name USING utf8mb4) REGEXP '^[가-힣]' THEN 0 ELSE 1 END, " +
            "CONVERT(name USING utf8mb4) COLLATE utf8mb4_general_ci", nativeQuery = true)
    List<String> findAllTagNames();

    Optional<Tag> findByName(String name);
}

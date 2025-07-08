package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    //@Query("SELECT t.name FROM Tag t")
    @Query(value = "SELECT name FROM tag ORDER BY CONVERT(name USING utf8mb4) COLLATE utf8mb4_unicode_ci", nativeQuery = true)
    List<String> findAllTagNames();

    Optional<Tag> findByName(String name);
}

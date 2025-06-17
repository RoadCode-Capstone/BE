package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT t.name FROM Tag t")
    List<String> findAllTagNames();
}

package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}

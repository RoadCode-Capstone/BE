package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByMember(Member member);
}

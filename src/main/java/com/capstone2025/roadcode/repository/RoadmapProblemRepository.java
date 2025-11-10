package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.RoadmapProblem;
import com.capstone2025.roadcode.entity.RoadmapProblemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapProblemRepository extends JpaRepository<RoadmapProblem, Long> {

    Optional<RoadmapProblem> findByRoadmapIdAndStatus(Long roadmapId, RoadmapProblemStatus status);

    List<RoadmapProblem> findByRoadmapId(Long roadmapId);

}

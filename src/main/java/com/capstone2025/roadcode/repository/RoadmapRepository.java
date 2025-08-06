package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Roadmap;
import com.capstone2025.roadcode.entity.RoadmapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    // 사용자가 가지고 있는 로드맵 목록 조회
    List<Roadmap> findByMember(Member member);

    // 사용자가 가지고 있는 로드맵 목록 조회(status 필터링)
    @Query("SELECT r FROM Roadmap r WHERE r.member = :member AND (:status IS NULL OR r.status = :status)")
    List<Roadmap> findRoadmapsByMemberAndStatus(
            @Param("member") Member member, @Param("status") RoadmapStatus status);
}

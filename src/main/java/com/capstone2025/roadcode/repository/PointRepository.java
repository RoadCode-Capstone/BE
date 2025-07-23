package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.dto.MemberPointRank;
import com.capstone2025.roadcode.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

//    // 출석 여부 체크 (중복 출석 방지)
//    boolean existsByMemberIdAndPointTypeAndCreatedAtBetween(
//            Long memberId,
//            PointType pointType,
//            LocalDateTime startDate,
//            LocalDateTime endDate
//    );

    // 출석 여부 체크 (중복 출석 방지)
    @Query("""
        SELECT COUNT(p) > 0 
        FROM Point p 
        WHERE p.pointType = 'ATTENDANCE'
        AND p.createdAt >= :startDate AND p.createdAt < :endDate
    """)
    boolean hasCheckedAttendanceToday(
            Long memberId, LocalDateTime startDate, LocalDateTime endDate
    );

    // 포인트 내역 조회
    List<Point> findAllByMemberIdAndCreatedAtBetween(
            Long memberId, LocalDateTime startDate, LocalDateTime endDate
    );

    // 포인트 총합
    @Query("SELECT sum(p.amount) " +
            "FROM Point p " +
            "WHERE p.member.id = :memberId " +
            "AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    int sumPointAmountByMemberId(
            Long memberId, LocalDateTime startDate, LocalDateTime endDate
    );

    // 사용자 간 전체 순위
    @Query("""
        SELECT new com.capstone2025.roadcode.dto.MemberPointRank(
            m.id,
            m.nickname,
            COALESCE(SUM(p.amount), 0)
        )
        FROM Member m
        LEFT JOIN Point p ON m.id = p.member.id AND p.createdAt >= :startDate AND p.createdAt < :endDate
        GROUP BY m.id, m.nickname
        ORDER BY COALESCE(SUM(p.amount), 0) DESC
    """)
    List<MemberPointRank> findMemberPointRanking(LocalDateTime startDate, LocalDateTime endDate);

//    @Query()
//    List<Point> findAllByMemberId();
}

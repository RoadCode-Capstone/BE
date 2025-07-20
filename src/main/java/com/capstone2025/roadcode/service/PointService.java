package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.PointType;
import com.capstone2025.roadcode.entity.Point;
import com.capstone2025.roadcode.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final MemberService memberService;

    // 출석 체크 및 포인트 지급
    @Transactional
    public String checkAttendance(String email) {

        Member member = memberService.findByEmail(email);
        LocalDateTime startDate = LocalDate.now().atStartOfDay(); // 오늘 00:00
        LocalDateTime endDate = startDate.plusDays(1); // 내일 00:00

        if(!pointRepository.hasCheckedAttendanceToday(member.getId(), startDate, endDate)) {
            Point point = Point.create(PointType.ATTENDANCE, member); // 출석
            pointRepository.save(point); // 출석 기록 저장
            return "출석에 성공했습니다.";
        } else {
            return "이미 출석 했습니다.";
        }
    }
}

package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.PointHistoryByDateResponse;
import com.capstone2025.roadcode.dto.PointHistoryByTypeResponse;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.PointType;
import com.capstone2025.roadcode.entity.Point;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 날짜 별 포인트 내역 조회
    public PointHistoryByDateResponse getPointHistoryByDate(String email, String start, String end){

        Member member = memberService.findByEmail(email); // 로그인 사용자 가져오기

        try{
            LocalDateTime startDate = LocalDate.parse(start).atStartOfDay(); // 시작 날짜 00:00
            LocalDateTime endDate = LocalDate.parse(end).plusDays(1).atStartOfDay(); // 끝 날짜 다음날 00:00

            List<Point> points = pointRepository.findAllByMemberIdAndCreatedAtBetween(member.getId(), startDate, endDate);
            return PointHistoryByDateResponse.from(points);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }

    // 종류 별 포인트 내역 조회
    public PointHistoryByTypeResponse getPointHistoryByType(String email, String start, String end){

        Member member = memberService.findByEmail(email); // 로그인 사용자 가져오기

        try{
            LocalDateTime startDate = LocalDate.parse(start).atStartOfDay(); // 시작 날짜 00:00
            LocalDateTime endDate = LocalDate.parse(end).plusDays(1).atStartOfDay(); // 끝 날짜 다음날 00:00

            List<Point> points = pointRepository.findAllByMemberIdAndCreatedAtBetween(member.getId(), startDate, endDate);
            return PointHistoryByTypeResponse.from(points);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }
}

package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.*;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.PointType;
import com.capstone2025.roadcode.entity.Point;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final MemberService memberService;

    // 출석 체크 및 포인트 지급
    @Transactional
    public String checkAttendance(String email) {

        Member member = memberService.findByEmail(email);
        LocalDateTime startDate = LocalDate.now().atStartOfDay(); // 오늘 00:00
        LocalDateTime endDate = startDate.plusDays(1); // 내일 00:00

        if(pointRepository.hasCheckedAttendanceToday(member.getId(), startDate, endDate)) {
            return "이미 출석 했습니다.";
        } else {
            Point point = Point.create(PointType.ATTENDANCE, member); // 출석
            pointRepository.save(point); // 출석 기록 저장
            return "출석에 성공했습니다.";
        }
    }

    // 리뷰 작성 포인트 지급
    public void giveCommentPoint(Member member) {

        Point point = Point.create(PointType.REVIEW, member); // 출석
        pointRepository.save(point); // 리뷰 포인트 기록 저장

    }

    // 문제 성공 포인트 지급
    public void giveSolutionPoint(Member member) {

        log.info("문제 성공 포인트 지급");

        Point point = Point.create(PointType.PROBLEM_SOLVED, member); // 출석
        pointRepository.save(point); // 기록 저장

    }

    // 일일학습목표 달성 포인트 지급
    public void giveDailyGoalPoint(Member member) {

        Point point = Point.create(PointType.DAILY_GOAL_COMPLETED, member); // 출석
        pointRepository.save(point); // 기록 저장

    }

    // 로드맵 달성 포인트 지급
    public void giveRoadmapPoint(Member member) {

        Point point = Point.create(PointType.ROADMAP_COMPLETED, member); // 출석
        pointRepository.save(point); // 기록 저장

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

    // 순위(랭킹) 조회
    public MemberPointRanking getPointRanking(String email, String start, String end) {

        Member member = memberService.findByEmail(email); // 로그인 사용자 가져오기

        try{
            LocalDateTime startDate = LocalDate.parse(start).atStartOfDay(); // 시작 날짜 00:00
            LocalDateTime endDate = LocalDate.parse(end).plusDays(1).atStartOfDay(); // 끝 날짜 다음날 00:00

            List<MemberPointRank> rankedList = pointRepository.findMemberPointRanking(startDate, endDate);

            // 순위 계산
            int rank = 1;
            rankedList.get(0).setRank(1); // 첫번째 순위
            for(int i = 1; i < rankedList.size(); i++){

                MemberPointRank preRank = rankedList.get(i-1); // 앞 순위
                MemberPointRank curRank = rankedList.get(i); // 현재 순위

                // 앞 순위와 포인트가 같을 경우 순위 동일
                if(preRank.getTotalPoint().equals(curRank.getTotalPoint())) {
                    curRank.setRank(rank);
                } else { // 동일하지 않은 경우, 인덱스+1 을 순위로 함
                    rank = i + 1;
                    rankedList.get(i).setRank(rank);
                }
            }

            // 내 순위 가져오기
            MemberPointRank myRank =
                    rankedList.stream().filter(r -> r.getMemberId().equals(member.getId())).findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


            return new MemberPointRanking(myRank.getRank(), rankedList);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }
}

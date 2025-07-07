package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.CommentCreateRequest;
import com.capstone2025.roadcode.dto.ReviewCreateRequest;
import com.capstone2025.roadcode.entity.Comment;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Review;
import com.capstone2025.roadcode.entity.Submission;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.CommentRepository;
import com.capstone2025.roadcode.repository.ReviewRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberService memberService;
    private final CommentRepository commentRepository;
    private final SubmissionService submissionService;

    @Transactional
    public void createReview(String email, Long submissionId, ReviewCreateRequest reviewCreateRequest) {
        Member member = memberService.findByEmail(email);
        Submission submission = submissionService.findById(submissionId);

        // 사용자가 해당 풀이의 문제를 풀었는지 확인 (리뷰를 작성할 수 있는 자격이 있는지)
        Long memberId = member.getId();
        Long problemId = submission.getProblem().getId();
        submissionService.validateSolvedProblem(memberId, problemId);

        // 자기 자신의 풀이에는 리뷰를 달 수 없음
        if(submission.getMember().getId() == memberId) {
            throw new CustomException(ErrorCode.REVIEW_SELF_ACCESS_DENIED);
        }

        // AI 리뷰 검사

        // 리뷰 저장
        Review review = Review.create(submission, member, reviewCreateRequest.getContent());
        reviewRepository.save(review);
    }

    @Transactional
    public void createComment(String email, Long reviewId, CommentCreateRequest commentCreateRequest) {
        Member member = memberService.findByEmail(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 사용자가 해당 풀이의 문제를 풀었는지 확인 (리뷰를 작성할 수 있는 자격이 있는지)
        Long memberId = member.getId();
        Long problemId = review.getSubmission().getProblem().getId();
        submissionService.validateSolvedProblem(memberId, problemId);

        // AI 답글 검사

        // 답글 저장
        Comment comment = Comment.create(review, member, commentCreateRequest.getContent());
        commentRepository.save(comment);
    }

}

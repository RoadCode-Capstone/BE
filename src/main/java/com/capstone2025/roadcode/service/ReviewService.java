package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.CommentCreateRequest;
import com.capstone2025.roadcode.dto.ReviewCreateRequest;
import com.capstone2025.roadcode.dto.ReviewListResponse;
import com.capstone2025.roadcode.dto.ReviewWithCommentsResponse;
import com.capstone2025.roadcode.entity.Comment;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Review;
import com.capstone2025.roadcode.entity.Submission;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.CommentRepository;
import com.capstone2025.roadcode.repository.MemberRepository;
import com.capstone2025.roadcode.repository.ReviewRepository;
import com.mysql.cj.x.protobuf.MysqlxCursor;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberService memberService;
    private final CommentRepository commentRepository;
    private final SubmissionService submissionService;
    private final OpenAIService aiService;

    private static final long AI_MEMBER_ID = 1L;
    private final MemberRepository memberRepository;

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
        String reviewContent = reviewCreateRequest.getContent(); // 리뷰 내용
        String problemDescription = submission.getProblem().getDescription(); // 문제 설명
        String sourceCode = submission.getSourceCode(); // 문제 풀이 코드
        if(!aiService.isValidReview(reviewContent, problemDescription, sourceCode)){
            throw new CustomException(ErrorCode.INVALID_REVIEW);
        }

        // 리뷰 저장
        Review review = Review.create(submission, member, reviewContent);
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
        String reviewContent = review.getContent(); // 리뷰 내용
        String commentContent = commentCreateRequest.getContent(); // 답글 내용
        if(!aiService.isValidComment(reviewContent, commentContent)){
            throw new CustomException(ErrorCode.INVALID_COMMENT);
        }

        // 답글 저장
        Comment comment = Comment.create(review, member, commentCreateRequest.getContent());
        commentRepository.save(comment);
    }

    public ReviewListResponse getAllReviewsBySubmissionIdWithComments(
            String email, Long submissionId
    ) {
        Member member = memberService.findByEmail(email);
        Submission submission = submissionService.findById(submissionId);

        // 사용자가 해당 풀이의 문제를 풀었는지 확인 (리뷰를 작성할 수 있는 자격이 있는지)
        Long memberId = member.getId();
        Long problemId = submission.getProblem().getId();
        submissionService.validateSolvedProblem(memberId, problemId);

        List<Review> reviews = reviewRepository.findAllBySubmissionIdWithComments(submissionId);

        List<ReviewWithCommentsResponse> reviewResponse = reviews.stream()
                .map(ReviewWithCommentsResponse::from)
                .collect(Collectors.toList());

        return new ReviewListResponse(reviewResponse);
    }

    // 임시 함수. 문제 풀이 성공했을 시 이 함수를 코드에 넣으면 될듯?
    @Transactional
    public void createAICodeReview(Long submissionId) {
        Submission submission = submissionService.findById(submissionId);
        String aiResponse = aiService.getAICodeReview(submission.getProblem(), submission.getSourceCode());

        // db 조회없이 ai 사용자의 프록시 객체를 가져옴 (select 쿼리 발생 x)
        Member aiMember = memberRepository.getReferenceById(AI_MEMBER_ID);

        // 리뷰 저장
        Review review = Review.create(submission, aiMember, aiResponse);
        reviewRepository.save(review);
    }
}

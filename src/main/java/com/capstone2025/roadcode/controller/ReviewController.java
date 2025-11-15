package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.CommentCreateRequest;
import com.capstone2025.roadcode.dto.ReviewCreateRequest;
import com.capstone2025.roadcode.dto.ReviewListResponse;
import com.capstone2025.roadcode.service.OpenAIService;
import com.capstone2025.roadcode.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submissions/{submissionId}/reviews")
    public ApiResponse<Void> createReview(Authentication authentication, @PathVariable Long submissionId, @RequestBody ReviewCreateRequest request) {

        String email = authentication.getName();
        reviewService.createReview(email, submissionId, request);
        return ApiResponse.successWithMessage("리뷰 작성에 성공했습니다.");
    }

    @PostMapping("/reviews/{reviewId}/comments")
    public ApiResponse<Void> createComment(Authentication authentication, @PathVariable Long reviewId, @RequestBody CommentCreateRequest request) {

        String email = authentication.getName();
        reviewService.createComment(email, reviewId, request);
        return ApiResponse.successWithMessage("답글 작성에 성공했습니다.");
    }

    @GetMapping("/submissions/{submissionId}/reviews-with-comments")
    public ApiResponse<ReviewListResponse> getReviewsWithComments(Authentication authentication, @PathVariable Long submissionId){

        String email = authentication.getName();
        return ApiResponse.success(reviewService.getAllReviewsBySubmissionIdWithComments(email, submissionId));
    }

//    @PostMapping("/submissions/{submissionId}/ai-reviews")
//    public ApiResponse<Void> createAIReview(@PathVariable Long submissionId) {
//
//        reviewService.createAICodeReview(submissionId);
//        return ApiResponse.successWithMessage("ai 리뷰 작성에 성공했습니다.");
//    }
}

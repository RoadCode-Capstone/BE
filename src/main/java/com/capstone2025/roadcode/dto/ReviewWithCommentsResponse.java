package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReviewWithCommentsResponse {

    private Long reviewId;
    private Long memberId;
    private String content;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;

    public static ReviewWithCommentsResponse from(Review review) {

        List<CommentResponse> comments = review.getComments().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return new ReviewWithCommentsResponse(
                review.getId(),
                review.getMember().getId(),
                review.getContent(),
                comments,
                review.getCreatedAt()
        );
    }
}

package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CommentResponse {
    private Long commentId;
    private Long memberId;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}

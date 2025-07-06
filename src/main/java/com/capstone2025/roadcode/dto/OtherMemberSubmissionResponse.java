package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Submission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OtherMemberSubmissionResponse {
    private Long submissionId;
    private String sourceCode;
    private String nickname;
    private LocalDateTime createdAt;

    public static OtherMemberSubmissionResponse from(Submission submission){
        return new OtherMemberSubmissionResponse(
                submission.getId(),
                submission.getSourceCode(),
                submission.getMember().getNickname(),
                submission.getCreatedAt()
        );
    }
}

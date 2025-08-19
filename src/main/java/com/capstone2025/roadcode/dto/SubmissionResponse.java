package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.entity.Submission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
    풀이 조회 관련 dto
 **/
public class SubmissionResponse {

    private Long id; // 풀이 아이디
    private Long problemId; // 문제 아이디
    private Long memberId; // 사용자 아이디
    private String sourceCode; // 제출한 소스 코드
    private LanguageType language; // 사용한 언어
    private boolean isSuccess; // 성공 여부

    public static SubmissionResponse from(Submission submission){
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getMember().getId(),
                submission.getSourceCode(),
                submission.getLanguage(),
                submission.isSuccess()
        );
    }
}

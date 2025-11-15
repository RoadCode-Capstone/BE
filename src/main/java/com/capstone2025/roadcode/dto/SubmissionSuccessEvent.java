package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 문제 풀이 성공 이벤트 발생
@Getter
@AllArgsConstructor
public class SubmissionSuccessEvent {
    private final Long submissionId;
}

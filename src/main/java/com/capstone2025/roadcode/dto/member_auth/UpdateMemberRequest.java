package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateMemberRequest {
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;
}

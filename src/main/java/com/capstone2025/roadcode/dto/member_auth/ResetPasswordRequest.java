package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    private String newPassword;
}

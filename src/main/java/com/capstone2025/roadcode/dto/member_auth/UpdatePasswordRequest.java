package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;
    @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    private String newPassword;
}

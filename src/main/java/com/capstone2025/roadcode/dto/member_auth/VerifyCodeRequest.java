package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VerifyCodeRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "인증코드를 입력해주세요.")
    private String verificationCode;
}

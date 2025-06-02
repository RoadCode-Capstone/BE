package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CheckEmailRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
}

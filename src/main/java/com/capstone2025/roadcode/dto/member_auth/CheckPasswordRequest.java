package com.capstone2025.roadcode.dto.member_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CheckPasswordRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

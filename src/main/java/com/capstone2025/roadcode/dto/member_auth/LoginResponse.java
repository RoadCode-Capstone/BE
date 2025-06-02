package com.capstone2025.roadcode.dto.member_auth;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    // private String refreshToken;

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}

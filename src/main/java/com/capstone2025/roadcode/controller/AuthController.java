package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.member_auth.*;
import com.capstone2025.roadcode.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignUpRequest requestDto) {
        authService.signup(requestDto);
        return ApiResponse.successWithMessage("회원가입 성공");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest requestDto) {
        return ApiResponse.success(authService.login(requestDto));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.successWithMessage("로그아웃 성공");
    }

    // 이메일 인증 요청 - 회원가입
    @PostMapping("/signup/verify-email")
    public ApiResponse<Void> sendSignupVerificationEmail(@Valid @RequestBody CheckEmailRequest checkEmailRequest){
        authService.requestSignupEmailVerification(checkEmailRequest);
        return ApiResponse.successWithMessage("인증코드가 이메일로 전송되었습니다.");
    }

    // 이메일 인증 요청 - 비밀번호 재설정
    @PostMapping("/reset-password/verify-email")
    public ApiResponse<Void> sendResetPasswordVerificationEmail(@Valid @RequestBody CheckEmailRequest checkEmailRequest){
        authService.requestResetPasswordEmailVerification(checkEmailRequest);
        return ApiResponse.successWithMessage("인증코드가 이메일로 전송되었습니다.");
    }

    // 인증 코드 확인
    @PostMapping("/verify-code")
    public ApiResponse<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request);
        return ApiResponse.successWithMessage("인증코드가 일치합니다.");
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.successWithMessage("비밀번호 재설정 완료");
    }
}

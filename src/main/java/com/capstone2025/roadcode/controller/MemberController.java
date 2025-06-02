package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.dto.member_auth.*;
import com.capstone2025.roadcode.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping()
    public ApiResponse<MemberInfoResponse> getMyInfo(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(memberService.getMyInfo(email));
    }

    @PutMapping()
    public ApiResponse<Void> updateMember(
            @RequestBody @Valid UpdateMemberRequest requestDto,
            Authentication authentication) {
        String email = authentication.getName();
        memberService.updateMember(requestDto, email);
        return ApiResponse.successWithMessage("회원 정보 수정 완료");
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest requestDto,
            Authentication authentication) {
        String email = authentication.getName();
        memberService.updatePassword(email, requestDto);
        return ApiResponse.successWithMessage("비밀번호 변경 완료");
    }

    @DeleteMapping()
    public ApiResponse<Void> deleteMember(
            @Valid @RequestBody DeleteMemberRequest requestDto,
            Authentication authentication) {
        String email = authentication.getName();
        memberService.deleteMember(requestDto, email);
        return ApiResponse.successWithMessage("회원탈퇴 완료");
    }

    @PostMapping("/verify-password")
    public ApiResponse<Void> verifyPassword(
            @Valid @RequestBody CheckPasswordRequest checkPasswordRequest,
            Authentication authentication) {
        memberService.verifyPassword(authentication.getName(), checkPasswordRequest);
        return ApiResponse.successWithMessage("비밀번호 확인 완료");
    }

    @GetMapping("/exists-email")
    public ApiResponse<CheckEmailDuplicatedResponse> checkEmailDuplicate(@RequestParam String email){
        return ApiResponse.success(new CheckEmailDuplicatedResponse(memberService.isEmailDuplicate(email)));
    }

    @GetMapping("/exists-nickname")
    public ApiResponse<CheckNicknameDuplicatedResponse> checkNicknameDuplicate(@RequestParam String nickname){
        return ApiResponse.success(new CheckNicknameDuplicatedResponse(memberService.isNicknameDuplicate(nickname)));
    }
}

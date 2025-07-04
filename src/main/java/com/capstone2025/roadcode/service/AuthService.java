package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.member_auth.*;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final MemberService memberService;
    private final VerificationService verificationService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void signup(SignUpRequest requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();
        String nickname = requestDto.getNickname();

        // 이메일 + provider 중복체크
        if(memberService.isEmailDuplicate(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // nickname 중복체크
        if(memberService.isNicknameDuplicate(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 인증코드 검증했는지 확인
        if(!verificationService.isVerified(email)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_SIGNUP);
        }

        // Member 객체 생성
        memberService.createMember(email, encodedPassword, nickname);

        verificationService.clearVerified(email);
    }


    // 로그인
    public LoginResponse login(LoginRequest requestDto) {

        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 객체 가져오기
        Member member = memberService.findByEmail(email);

        // 비번 맞는지 확인
        if (!passwordEncoder.matches(password, member.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(member.getEmail(), member.getRole());
        return new LoginResponse(accessToken);
    }

    // 회원가입용 이메일 인증 요청
    public void requestSignupEmailVerification(CheckEmailRequest checkEmailRequest) {
        String email = checkEmailRequest.getEmail();
        // 이메일 중복 검사
        boolean isEmailDuplicate = memberService.isEmailDuplicate(email);
        if(isEmailDuplicate) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 인증 코드 생성/저장
        String code = verificationService.generateAndSaveCode(email);
        log.info("인증 코드: {}", code);

        // 인증 코드 메일로 전송
        mailService.sendCode(email, code);
        log.info("인증코드 메일로 전송 완.");
    }

    // 비밀번호 재설정용 이메일 인증 요청
    public void requestResetPasswordEmailVerification(CheckEmailRequest checkEmailRequest) {
        String email = checkEmailRequest.getEmail();
        // 사용자 존재 여부 검사
        boolean isExistingEmail = memberService.isActiveEmail(email);
        if(!isExistingEmail) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 인증 코드 생성/저장
        String code = verificationService.generateAndSaveCode(email);

        // 인증 코드 메일로 전송
        mailService.sendCode(email, code);
    }

    // 인증 코드 검증
    public void verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getVerificationCode();
        verificationService.verifyCode(email, code);
    }

    // 비밀번호 재설정
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String password = request.getNewPassword();

        // 인증코드 검증 완료한 회원인지 확인
        boolean isVerified = verificationService.isVerified(email);
        if(!isVerified) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_PASSWORD_RESET);
        }

        // 비밀번호 재설정
        memberService.resetPassword(email, password);

        // 인증 상태 삭제
        verificationService.clearVerified(email);
    }
}

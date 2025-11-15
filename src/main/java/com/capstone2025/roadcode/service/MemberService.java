package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.member_auth.*;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.entity.AuthProvider;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 생성
    @Transactional
    public Member createMember(String email, String encodedPassword, String nickname) {
        Member member = Member.localCreate(email, encodedPassword, nickname);
        memberRepository.save(member);
        return member;
    }

    // 회원 객체 가져오기
    public Member findByEmail(String email) {
        return memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public MemberInfoResponse getMyInfo(String email) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new MemberInfoResponse(member);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMember(UpdateMemberRequest requestDto, String email) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String nickname = requestDto.getNickname();

        if(memberRepository.existsByNicknameAndIsDeletedFalseAndIdNot(nickname, member.getId())){
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        member.updateNickname(requestDto.getNickname());
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(DeleteMemberRequest requestDto, String email) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인(서버에서 비밀번호 재확인 과정)
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

//        memberRepository.delete(member);
        member.markDeleted(); // soft delete
    }

    // 비밀번호 재확인
    public void verifyPassword(String email, CheckPasswordRequest checkPasswordRequest) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String rawPassword = checkPasswordRequest.getPassword();
        if(!passwordEncoder.matches(rawPassword, member.getPassword())){
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest requestDto) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
// valid 처리
//        if (requestDto.getCurrentPassword() == null || requestDto.getCurrentPassword().isBlank()) {
//            throw new InvalidPasswordException("현재 비밀번호를 입력해야합니다.");
//        }
//        if (requestDto.getNewPassword() == null || requestDto.getNewPassword().isBlank()) {
//            throw new InvalidPasswordException("새 비밀번호를 입력해야합니다.");
//        }

        // 현재 비밀번호 확인(서버에서 비밀번호 재확인 과정)
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())){
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호가 기존과 동일한 경우
        if(passwordEncoder.matches(requestDto.getNewPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.SAME_PASSWORD);
        }
        member.updatePassword(requestDto.getNewPassword(), passwordEncoder);
    }

    // 이메일 중복 체크 (회원가입시)
    // 탈퇴한 회원 메일 조회 -> 사용 불가
    public boolean isEmailDuplicate(String email) {
        if (memberRepository.existsByEmailAndProvider(email, AuthProvider.LOCAL)) {
            return true;
        }
        return false;
    }

    // 닉네임 중복 체크
    public boolean isNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNicknameAndIsDeletedFalse(nickname)) {
            return true;
        }
        return false;
    }

    // 사용자 존재 여부 체크 (비밀번호 재설정시)
    // 탈퇴한 회원 메일 조회 안함
    public boolean isActiveEmail(String email) {
        if (memberRepository.existsByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)) {
            return true;
        }
        return false;
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.updatePassword(newPassword, passwordEncoder);
    }

    // 회원 객체 가져오기
    public Member findById(Long memberId) {
        return memberRepository.findByIdAndProviderAndIsDeletedFalse(memberId, AuthProvider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}

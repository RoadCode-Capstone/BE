package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.entity.AuthProvider;
import com.capstone2025.roadcode.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByEmailAndProviderAndIsDeletedFalse(String email, AuthProvider provider);

    // 가입 시, 이메일 체크
    boolean existsByEmailAndProvider(String email, AuthProvider provider);

    // 비밀번호 재설정 시, 이메일 체크 (사용자 존재 여부 확인)
    boolean existsByEmailAndProviderAndIsDeletedFalse(String email, AuthProvider provider);

    boolean existsByNicknameAndIsDeletedFalse(String nickname);

    boolean existsByNicknameAndIsDeletedFalseAndIdNot(String nickname, Long memberId);
}

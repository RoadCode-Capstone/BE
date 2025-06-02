package com.capstone2025.roadcode.security;

import com.capstone2025.roadcode.entity.AuthProvider;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmailAndProviderAndIsDeletedFalse(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new MemberDetails(member);
    }
}

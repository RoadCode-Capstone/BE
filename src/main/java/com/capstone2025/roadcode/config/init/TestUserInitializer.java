package com.capstone2025.roadcode.config.init;

import com.capstone2025.roadcode.entity.AuthProvider;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
public class TestUserInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUserInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.existsByEmailAndProviderAndIsDeletedFalse("test@example.com", AuthProvider.LOCAL)) return;

        Member testUser = Member.localCreate("test@example.com", passwordEncoder.encode("1234"), "테스트유저");

        memberRepository.save(testUser);
    }
}

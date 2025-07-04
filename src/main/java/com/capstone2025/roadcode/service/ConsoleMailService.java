package com.capstone2025.roadcode.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
@Slf4j
public class ConsoleMailService implements MailService {

    @Override
    public void sendCode(String toEmail, String code) {
        log.info("[로컬 테스트] 메일 대상 : {}, 인증코드: {}", toEmail, code);
    }
}

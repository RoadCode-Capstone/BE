package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.exception.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!local")
@Slf4j
public class HtmlMailService implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public void sendCode(String toEmail, String code) {
        try {
            JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
            log.info("Host: {}", impl.getHost());
            log.info("Port: {}", impl.getPort());
            log.info("Username: {}", impl.getUsername());
            log.info("Password: {}", impl.getPassword()); // 프로덕션에선 주의!

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("hiii717@naver.com");
            helper.setTo(toEmail);
            helper.setSubject("회원가입 인증 코드");
            helper.setText("인증 코드: " + code + "\n5분 안에 입력해 주세요.");

            log.info("메일 안 인증 코드: {}", code);

            mailSender.send(message);
            log.info("send message 됨");

        } catch (MessagingException e) {
            log.error("메일 전송 실패", e);
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}

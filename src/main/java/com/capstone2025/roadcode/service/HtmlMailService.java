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

    public void sendCode(String toEmail, String code) {
        try {
            JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setText("인증 코드: " + code + "\n5분 안에 입력해 주세요.");

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}

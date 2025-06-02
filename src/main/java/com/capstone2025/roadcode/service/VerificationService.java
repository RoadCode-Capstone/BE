package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration VERIFICATION_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration VERIFIED_USER_EXPIRATION = Duration.ofMinutes(15);
    private static final String RESET_AUTH_PREFIX = "reset_auth:";

    public String generateAndSaveCode(String email) {

        int randomNumber = new Random().nextInt(1_000_000);
        DecimalFormat df = new DecimalFormat("000000");
        String code = df.format(randomNumber);
        redisTemplate.opsForValue().set(email, code, VERIFICATION_EXPIRATION);

        return code;
    }

    public void verifyCode(String email, String code) {

        String storedCode = redisTemplate.opsForValue().get(email);
        if(storedCode == null || !storedCode.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증 성공 -> 인증 상태 저장
        String authKey = RESET_AUTH_PREFIX + email;
        redisTemplate.opsForValue().set(authKey, "true", VERIFIED_USER_EXPIRATION);

        // 인증코드 삭제(무효화)
        redisTemplate.delete(email);
    }

    public boolean isVerified(String email) {
        String key = RESET_AUTH_PREFIX + email;
        return Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(key));
    }

    public void clearVerified(String email) {
        redisTemplate.delete(RESET_AUTH_PREFIX + email);
    }
}

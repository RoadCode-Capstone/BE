package com.capstone2025.roadcode.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 시크릿 키 (32 바이트 이상 필요)
    private final SecretKey key = Jwts.SIG.HS256.key().build(); //or HS384.key() or HS512.key()

    // 토큰 유효 시간(24시간)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1000ms * 60초 * 60분 * 24시간 -> 하루

    // 토큰 생성
//    public String createToken(String username) {
//        return Jwts.builder()
//                .subject(username)
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(key)
//                .compact();
//    }
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // 토큰 파싱
    public String getUsernameFromToken(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    // 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 파싱 (내부용)
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}

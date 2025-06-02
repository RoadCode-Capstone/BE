package com.capstone2025.roadcode.security.jwt;

import com.capstone2025.roadcode.security.MemberDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberDetailsService memberDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Authentication 헤더에서 토큰 꺼내기
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 이후부터

            // 토큰 유효성 검사
            if (jwtUtil.validateToken(token)){
                // 토큰에서 username 꺼내기
                String username = jwtUtil.getUsernameFromToken(token);

                // SecurityContext에 이미 인증된 정보가 없다면
                if(SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 유저 정보 조회
                    UserDetails userDetails = memberDetailsService.loadUserByUsername(username);

                    // Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // SecurityContext 에 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}

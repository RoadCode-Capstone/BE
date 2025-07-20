package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberPointRankResponse {

    private Long memberId; // 사용자 아이디
    private String nickname; // 사용자 닉네임
    private Long totalPoint; // 총 포인트(특정 기간 동안의)

    public MemberPointRankResponse(Long memberId, String nickname, Long totalPoint) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.totalPoint = totalPoint;
    }
}

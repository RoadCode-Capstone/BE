package com.capstone2025.roadcode.dto.member_auth;

import com.capstone2025.roadcode.entity.Member;
import lombok.Getter;

@Getter
public class MemberInfoResponse {
    private String email;
    private String nickname;
    //private int point;

    public MemberInfoResponse(Member member) {
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        //this.point = member.getPoint();
    }
}

package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointRanking {

    private int myRank; // 내 순위
    private List<MemberPointRank> ranks; // 사용자 순위 랭킹 목록
}

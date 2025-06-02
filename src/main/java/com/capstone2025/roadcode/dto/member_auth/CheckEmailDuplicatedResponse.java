package com.capstone2025.roadcode.dto.member_auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckEmailDuplicatedResponse {
    private boolean duplicated;
}

package com.capstone2025.roadcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LevelTestResultResponse {
    private int totalProblems;
    private List<Boolean> result = new ArrayList<>();
    private int passedCount;
}

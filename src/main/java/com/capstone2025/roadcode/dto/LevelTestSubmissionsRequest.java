package com.capstone2025.roadcode.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LevelTestSubmissionsRequest {
    private List<SubmitLevelTestRequest> submissions = new ArrayList<>();
}

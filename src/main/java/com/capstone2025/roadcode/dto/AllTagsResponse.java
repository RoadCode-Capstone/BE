package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class AllTagsResponse {
    private List<String> tags = new ArrayList<>();

    public static AllTagsResponse from(Problem problem) {
        return new AllTagsResponse(
                problem.getProblemTags().stream()
                .map(pt -> pt.getTag().getName())
                .collect(Collectors.toList())
        );
    }
}

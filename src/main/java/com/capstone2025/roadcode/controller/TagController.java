package com.capstone2025.roadcode.controller;

import com.capstone2025.roadcode.common.ApiResponse;
import com.capstone2025.roadcode.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping()
    public ApiResponse<List<String>> getAllTags() {
        return ApiResponse.success(tagService.getAllTagNames());
    }
}

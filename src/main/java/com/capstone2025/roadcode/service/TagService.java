package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.AllTagsResponse;
import com.capstone2025.roadcode.entity.Tag;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public AllTagsResponse getAllTagNames() {
        return new AllTagsResponse(tagRepository.findAllTagNames());
    }

    public boolean containsTag(String algorithm){
        return tagRepository.findAllTagNames().contains(algorithm);
    }

    public Tag findByName(String algorithm) {
        return tagRepository.findByName(algorithm)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_TAG_NOT_FOUND));
    }
}

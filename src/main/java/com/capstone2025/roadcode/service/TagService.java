package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.AllTagsResponse;
import com.capstone2025.roadcode.entity.Tag;
import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import com.capstone2025.roadcode.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    private static final int PROBLEM_COUNT = 30;

    // 문제 개수가 30개 이상인 태그 이름만 출력
    public AllTagsResponse getTagNamesExceeds30() {
        return new AllTagsResponse(tagRepository.findTagNamesWithProblemCount(PROBLEM_COUNT));
    }

    public Tag findByName(String algorithm) {
        return tagRepository.findByName(algorithm)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_TAG_NOT_FOUND));
    }
}

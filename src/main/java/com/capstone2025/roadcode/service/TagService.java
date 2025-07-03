package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.dto.AllTagsResponse;
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
}

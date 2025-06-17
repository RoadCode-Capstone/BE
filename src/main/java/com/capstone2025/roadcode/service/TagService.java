package com.capstone2025.roadcode.service;

import com.capstone2025.roadcode.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<String> getAllTagNames() {
        return tagRepository.findAllTagNames();
    }
}

package com.capstone2025.roadcode.common;

import com.capstone2025.roadcode.exception.CustomException;
import com.capstone2025.roadcode.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
public enum LanguageType {
    // enum은 내부적으로 private final이 필드로 생성되는 하나의 클래스. 따라서 noArgsConstructor은 당연히 생성 불가. 무조건 필드안에 값이 있어야함. 그게 enum임
    PYTHON("python:3.11", ".py", "python /app/solution.py"),
    JAVA("openjdk:17", ".java", "javac /app/Solution.java && java Solution"),
    C("gcc", ".c", "gcc /app/solution.c -o /app/solution && /app/solution");

    private final String image;
    private final String extension;
    private final String command;

    LanguageType(String image, String extension, String command) {
        this.image = image;
        this.extension = extension;
        this.command = command;
    }

    public static LanguageType fromString(String language) {
        return Arrays.stream(values())
                .filter(l -> l.name().equalsIgnoreCase(language))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LANGUAGE_TYPE));
    }
}

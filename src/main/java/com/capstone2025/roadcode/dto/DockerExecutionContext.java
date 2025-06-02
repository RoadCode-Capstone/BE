package com.capstone2025.roadcode.dto;

import com.capstone2025.roadcode.common.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DockerExecutionContext {
    private LanguageType languageType;
    private String codeDir;
//    private String language;
//    private String sourceCode;
//    private String dockerImage;
//    private String codeFilePath;
//    private String executeCommand;
//    private String codeDir;
}

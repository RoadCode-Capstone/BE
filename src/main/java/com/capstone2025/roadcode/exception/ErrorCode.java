package com.capstone2025.roadcode.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND("E001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MISSING_TOKEN("E002", "토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("E003", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_EMAIL("E004","이미 등록된 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("E005", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("E006", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED), // 로그인시(JWT 토큰 없음)
    SAME_PASSWORD("E007", "기존 비밀번호와 동일합니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("E008", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST), // 로그인한 상태에서 확인(JWT 토큰 있음)
    MAIL_SEND_FAILED("E009", "메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_VERIFICATION_CODE("E010", "인증코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_PASSWORD_RESET("E011", "유효하지 않은 비밀번호 재설정 요청입니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_SIGNUP("E012","이메일을 인증해야합니다.", HttpStatus.FORBIDDEN),
    TESTCASE_NOT_FOUND_FOR_PROBLEM("E013", "해당 문제에 테스트케이스가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    PROBLEM_NOT_FOUND("E014", "문제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_LANGUAGE_TYPE("E015", "지원하지 않는 언어입니다.", HttpStatus.BAD_REQUEST),
    FILE_WRITE_FAILED("E016", "소스 코드 파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("E017", "소스 코드 파일 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROADMAP_NOT_FOUND("E018", "로드맵을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ROADMAP_TYPE("E019", "지원하지 않는 로드맵 종류입니다.", HttpStatus.BAD_REQUEST),
    PROBLEM_TAG_NOT_FOUND("E020", "문제 태그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

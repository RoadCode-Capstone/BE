package com.capstone2025.roadcode.common;

import com.capstone2025.roadcode.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, data);
    }

    // 성공 응답 (메시지만 있고 데이터는 없는 경우)
    public static <T> ApiResponse<T> successWithMessage(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }

}

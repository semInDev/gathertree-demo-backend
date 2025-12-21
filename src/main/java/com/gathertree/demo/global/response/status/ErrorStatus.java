package com.gathertree.demo.global.response.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

    // ===== Common =====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500",
            "서버 에러가 발생했습니다."),

    // ===== Tree =====
    TREE_NOT_FOUND(HttpStatus.NOT_FOUND, "TREE_4040", "트리를 찾을 수 없습니다."),
    TREE_EXPIRED(HttpStatus.NOT_FOUND, "TREE_4041", "트리가 만료되었습니다."),

    // ===== Decoration =====
    DECORATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "DECO_4000",
            "장식은 최대 10개까지 가능합니다."),
    DECORATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DECO_4040",
            "장식을 찾을 수 없습니다."),

    // ===== Image =====
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "IMAGE_4000",
            "유효하지 않은 이미지 형식입니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_5000",
            "이미지 업로드에 실패했습니다."),

    // ===== AI Evaluation =====
    EVALUATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AI_4000",
            "장식이 10개 모두 모였을 때만 평가할 수 있습니다."),
    AI_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_5000",
            "AI 평가 중 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

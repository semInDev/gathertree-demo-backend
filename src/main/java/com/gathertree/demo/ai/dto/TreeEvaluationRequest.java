package com.gathertree.demo.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "트리 AI 평가 요청")
public class TreeEvaluationRequest {

    @Schema(
            description = """
            S3에 업로드된 트리 이미지 object key
            
            예시:
            - eval/tmp/abcd-1234.png
            """
    )
    @NotBlank(message = "평가할 이미지 key는 필수입니다.")
    private String imageKey;
}

package com.gathertree.demo.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "트리 AI 평가 요청")
public class TreeEvaluationRequest {

    @Schema(
            description = """
            AI 평가에 사용할 최종 트리 이미지 URL입니다.
            
            - 프론트엔드 Canvas에서 트리 + 장식을 합성한 결과 이미지
            - 반드시 외부에서 접근 가능한(public) URL이어야 합니다.
            """,
            example = "https://cdn.beour.store/eval/tmp/abcd-1234.png"
    )
    @NotBlank(message = "평가할 이미지 URL은 필수입니다.")
    private String imageUrl;
}

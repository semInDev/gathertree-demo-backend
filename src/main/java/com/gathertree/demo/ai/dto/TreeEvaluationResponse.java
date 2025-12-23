package com.gathertree.demo.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "트리 AI 평가 결과")
public class TreeEvaluationResponse {

    @Schema(description = "AI가 매긴 트리 점수 (0~100)", example = "32")
    private int score;

    @Schema(description = "AI가 지어준 트리 제목", example = "우정 점검표에서 빨간 줄 그어진 트리")
    private String title;

    @Schema(
            description = "트리에 대한 한 줄 요약 평가",
            example = "함께 만들었다기보단 서로 눈치 안 보고 각자 욕심을 던진 결과물이에요. 이 트리는 협업의 실패 사례로 더 설득력이 있어."
    )
    private String summary;

    @Schema(
            description = "AI의 세부 코멘트 목록",
            example = "[\"미술 실력 이전에 이 정도면 됐지라는 태도가 트리 전체를 지배하고 있어.\", \"이 트리는 완성도가 낮아서가 아니라, 서로 맞춰볼 생각 자체가 없어서 이렇게 된 게 더 아프다.\", \"같이 만든 트리라기보단, 우정이 아직 잘 작동하는지 테스트해본 결과물 같아.\", \"같이 만든 트리라기보단 각자 대충 손 얹고 우정은 자동 저장될 거라 믿은 결과 같아.\"]"
    )
    private List<String> comments;

    @Schema(
            description = "평가 완료 후 public으로 공개된 최종 트리 이미지 URL",
            example = "https://cdn.beour.store/eval/public/abcd-1234.png"
    )
    private String imageUrl;
}

package com.gathertree.demo.tree.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "트리 조회 응답")
public class TreeResponse {

    @Schema(description = "트리 UUID")
    private String uuid;

    @Schema(description = "트리 베이스 이미지 URL")
    private String baseImageUrl;

    @Schema(description = "장식 개수")
    private int decorationCount;

    @Schema(description = "장식 목록")
    private List<DecorationResponse> decorations;

    @Schema(description = "트리 생성 시간")
    private LocalDateTime createdAt;
}


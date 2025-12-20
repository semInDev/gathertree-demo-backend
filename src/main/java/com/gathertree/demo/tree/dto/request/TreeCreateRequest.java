package com.gathertree.demo.tree.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "트리 생성 요청")
public class TreeCreateRequest {

    @Schema(
            description = "트리 이미지 (PNG base64)",
            example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
    )
    @NotBlank(message = "트리 이미지는 필수입니다.")
    private String imageBase64;
}

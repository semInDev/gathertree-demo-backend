package com.gathertree.demo.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Presigned URL 발급 응답")
public class PresignedUrlResponse {

    @Schema(
            description = "S3에 PUT 업로드 가능한 Presigned URL (임시)",
            example = "https://s3.ap-northeast-2.amazonaws.com/..."
    )
    private String uploadUrl;

    @Schema(
            description = "업로드 완료 후 접근 가능한 public 이미지 URL",
            example = "https://cdn.beour.store/eval/tmp/abcd-1234.png"
    )
    private String publicUrl;
}

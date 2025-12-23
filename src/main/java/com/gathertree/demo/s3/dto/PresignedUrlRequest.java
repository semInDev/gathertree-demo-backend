package com.gathertree.demo.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Presigned URL 발급 요청")
public class PresignedUrlRequest {

    @Schema(
            description = """
            S3에 업로드할 object key 경로
            
            예시:
            - eval/tmp/{uuid}.png
            """,
            example = "eval/tmp/abcd-1234.png"
    )
    @NotBlank(message = "S3 object key는 필수입니다.")
    private String key;
}

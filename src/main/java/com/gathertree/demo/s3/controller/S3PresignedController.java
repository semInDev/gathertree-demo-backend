package com.gathertree.demo.s3.controller;

import com.gathertree.demo.global.response.ApiResult;
import com.gathertree.demo.s3.dto.PresignedUrlRequest;
import com.gathertree.demo.s3.dto.PresignedUrlResponse;
import com.gathertree.demo.s3.service.S3PresignedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "S3",
        description = "S3 Presigned URL 발급 API"
)
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3PresignedController {

    private final S3PresignedService s3PresignedService;

    @Operation(
            summary = "S3 Presigned PUT URL 발급",
            description = """
            프론트엔드에서 S3로 직접 이미지를 업로드하기 위한
            Presigned PUT URL을 발급합니다.
            
            - 업로드 유효 시간: 5분
            - 업로드 방식: HTTP PUT
            - Content-Type: image/png
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PresignedUrlResponse.class)
            )
    )
    @PostMapping("/presigned-url")
    public ApiResult<PresignedUrlResponse> issuePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        return ApiResult.onSuccess(
                s3PresignedService.generatePresignedPutUrl(request.getKey())
        );
    }
}

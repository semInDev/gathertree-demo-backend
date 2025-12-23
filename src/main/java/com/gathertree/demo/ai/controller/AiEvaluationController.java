package com.gathertree.demo.ai.controller;

import com.gathertree.demo.ai.dto.TreeEvaluationRequest;
import com.gathertree.demo.ai.dto.TreeEvaluationResponse;
import com.gathertree.demo.ai.service.AiEvaluationService;
import com.gathertree.demo.global.response.ApiResult;
import com.gathertree.demo.tree.model.Tree;
import com.gathertree.demo.tree.service.TreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "AI",
        description = "트리 AI 평가 API"
)
@RestController
@RequestMapping("/tree")
@RequiredArgsConstructor
public class AiEvaluationController {

    private final TreeService treeService;
    private final AiEvaluationService aiEvaluationService;

    @Operation(
            summary = "트리 AI 평가",
            description = """
            완성된 트리에 대해 AI 평가를 수행합니다.
            
            ▶ 평가 조건
            - 트리에 장식이 정확히 10개 이상 있어야 합니다.
            - AI 평가는 전체 서비스 기준 선착순 200회까지만 제공됩니다.
            - 같은 트리 + 같은 평가 모드(mild/spicy)는 캐시된 결과를 반환합니다.
            
            ▶ 평가 이미지
            - 프론트엔드에서 Canvas로 합성한 최종 트리 이미지 URL을 전달해야 합니다.
            - 해당 이미지는 외부(OpenAI)에서 접근 가능한 public URL이어야 합니다.
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "AI 평가 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreeEvaluationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "평가 조건 미충족 (장식 개수 부족, mode 오류, 선착순 초과)"
    )
    @ApiResponse(
            responseCode = "404",
            description = "트리를 찾을 수 없음"
    )
    @PostMapping("/{uuid}/evaluate")
    public ApiResult<TreeEvaluationResponse> evaluateTree(
            @Parameter(
                    description = "트리 UUID (관리 페이지 기준)",
                    example = "abcd-1234-efgh"
            )
            @PathVariable String uuid,

            @Parameter(
                    description = "평가 모드",
                    schema = @Schema(
                            allowableValues = {"mild", "spicy"},
                            example = "mild"
                    )
            )
            @RequestParam String mode,

            @Valid
            @RequestBody TreeEvaluationRequest request
    ) {
        Tree tree = treeService.getTreeEntity(uuid);

        return ApiResult.onSuccess(
                aiEvaluationService.evaluate(
                        tree,
                        mode,
                        request.getImageKey()
                )
        );
    }
}

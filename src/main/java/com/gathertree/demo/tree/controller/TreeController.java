package com.gathertree.demo.tree.controller;

import com.gathertree.demo.global.response.ApiResult;
import com.gathertree.demo.tree.dto.request.DecorationCreateRequest;
import com.gathertree.demo.tree.dto.request.DecorationReorderRequest;
import com.gathertree.demo.tree.dto.request.TreeCreateRequest;
import com.gathertree.demo.tree.dto.response.DecorationCreateResponse;
import com.gathertree.demo.tree.dto.response.TreeCreateResponse;
import com.gathertree.demo.tree.dto.response.TreeResponse;
import com.gathertree.demo.tree.service.TreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Tree",
        description = "트리 생성, 조회 및 장식 관리 API"
)
@RestController
@RequestMapping("/tree")
@RequiredArgsConstructor
public class TreeController {

    private final TreeService treeService;

    @Operation(
            summary = "트리 생성",
            description = """
        트리 주인이 최초 트리를 생성합니다.
        
        - 160×192 픽셀 트리 이미지(base64 PNG)를 업로드합니다.
        - 생성된 트리는 24시간 동안 유지됩니다.
        - 응답으로 트리 관리용 UUID를 반환합니다.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "트리 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TreeCreateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @PostMapping
    public ApiResult<TreeCreateResponse> createTree(
            @Valid @RequestBody TreeCreateRequest request
    ) {
        return ApiResult.onSuccess(treeService.createTree(request));
    }

    @Operation(
            summary = "트리 조회",
            description = """
        특정 트리를 조회합니다.
        
        - 트리 베이스 이미지 URL
        - 장식 목록 (orderIndex 기준 정렬)
        - 장식 개수 포함
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "트리 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "트리를 찾을 수 없음"
            )
    })
    @GetMapping("/{uuid}")
    public ApiResult<TreeResponse> getTree(@PathVariable String uuid) {
        return ApiResult.onSuccess(treeService.getTree(uuid));
    }

    @Operation(
            summary = "트리 수정",
            description = """
        트리 주인이 트리 베이스 이미지를 수정합니다.
        
        - 기존 장식은 유지됩니다.
        - base.png 파일만 덮어씁니다.
        """
    )
    @PutMapping("/{uuid}")
    public ApiResult<Void> updateTree(
            @PathVariable String uuid,
            @Valid @RequestBody TreeCreateRequest request
    ) {
        treeService.updateTree(uuid, request);
        return ApiResult.onSuccess(null);
    }

    @Operation(
            summary = "장식 추가",
            description = """
        친구가 트리에 장식을 추가합니다.
        
        - 32×32 픽셀 장식 이미지(base64 PNG)
        - 닉네임 포함
        - 최대 10개까지 가능
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "장식 추가 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "장식 개수 초과"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "트리를 찾을 수 없음"
            )
    })
    @PostMapping("/{uuid}/decorations")
    public ApiResult<DecorationCreateResponse> addDecoration(
            @PathVariable String uuid,
            @Valid @RequestBody DecorationCreateRequest request
    ) {
        return ApiResult.onSuccess(treeService.addDecoration(uuid, request));
    }

    @Operation(
            summary = "장식 순서 변경",
            description = """
        트리에 추가된 장식의 순서를 변경합니다.
        
        - 장식 ID 배열 순서대로 재배치됩니다.
        - orderIndex는 0부터 다시 부여됩니다.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "순서 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @PutMapping("/{uuid}/decorations/reorder")
    public ApiResult<Void> reorderDecorations(
            @PathVariable String uuid,
            @Valid @RequestBody DecorationReorderRequest request
    ) {
        treeService.reorderDecorations(uuid, request);
        return ApiResult.onSuccess(null);
    }

    @Operation(
            summary = "장식 삭제",
            description = """
        특정 장식을 삭제합니다.
        
        - 삭제된 장식은 복구되지 않습니다.
        - 남은 장식들은 orderIndex가 재정렬됩니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "장식을 찾을 수 없음")
    })
    @DeleteMapping("/{uuid}/decorations/{decorationId}")
    public ApiResult<Void> deleteDecoration(
            @PathVariable String uuid,
            @PathVariable String decorationId
    ) {
        treeService.deleteDecoration(uuid, decorationId);
        return ApiResult.onSuccess(null);
    }


}

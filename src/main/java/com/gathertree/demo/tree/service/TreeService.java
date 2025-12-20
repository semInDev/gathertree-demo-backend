package com.gathertree.demo.tree.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import com.gathertree.demo.global.util.Base64ImageUtil;
import com.gathertree.demo.image.service.S3ImageService;
import com.gathertree.demo.tree.dto.request.DecorationCreateRequest;
import com.gathertree.demo.tree.dto.request.DecorationReorderRequest;
import com.gathertree.demo.tree.dto.request.TreeCreateRequest;
import com.gathertree.demo.tree.dto.response.DecorationCreateResponse;
import com.gathertree.demo.tree.dto.response.DecorationResponse;
import com.gathertree.demo.tree.dto.response.TreeCreateResponse;
import com.gathertree.demo.tree.dto.response.TreeResponse;
import com.gathertree.demo.tree.model.Decoration;
import com.gathertree.demo.tree.model.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TreeService {

    private static final String TREE_KEY_PREFIX = "tree:";
    private static final int TREE_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final S3ImageService s3ImageService;

    /* =========================
       트리 생성
       ========================= */
    public TreeCreateResponse createTree(TreeCreateRequest request) {
        String uuid = UUID.randomUUID().toString();

        try {
            // 1. base64 → png
            byte[] imageBytes = Base64ImageUtil.decode(request.getImageBase64());

            // 2. S3 업로드
            String imageUrl = s3ImageService.upload(
                    imageBytes,
                    "trees/" + uuid + "/base.png"
            );

            // 3. Tree 객체 생성
            Tree tree = Tree.builder()
                    .uuid(uuid)
                    .baseImageUrl(imageUrl)
                    .decorations(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            saveToRedis(tree);

            return new TreeCreateResponse(uuid);

        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "트리 생성 중 오류가 발생했습니다."
            );
        }
    }

    /* =========================
       트리 조회
       ========================= */
    public TreeResponse getTree(String uuid) {
        Tree tree = getTreeFromRedis(uuid);

        try {
            List<DecorationResponse> decorations = tree.getDecorations().stream()
                    .sorted(Comparator.comparingInt(Decoration::getOrderIndex))
                    .map(d -> DecorationResponse.builder()
                            .id(d.getId())
                            .authorName(d.getAuthorName())
                            .orderIndex(d.getOrderIndex())
                            .imageUrl(d.getImageUrl())
                            .build())
                    .toList();

            return TreeResponse.builder()
                    .uuid(tree.getUuid())
                    .baseImageUrl(tree.getBaseImageUrl())
                    .decorationCount(decorations.size())
                    .decorations(decorations)
                    .createdAt(tree.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "트리 조회 중 오류가 발생했습니다."
            );
        }
    }

    /* =========================
       장식 추가
       ========================= */
    public DecorationCreateResponse addDecoration(
            String uuid,
            DecorationCreateRequest request
    ) {
        Tree tree = getTreeFromRedis(uuid);

        if (tree.getDecorations().size() >= 10) {
            throw new GeneralException(ErrorStatus.DECORATION_LIMIT_EXCEEDED);
        }

        try {
            String decorationId = UUID.randomUUID().toString();

            // 1. 이미지 업로드
            byte[] imageBytes = Base64ImageUtil.decode(request.getImageBase64());
            String imageUrl = s3ImageService.upload(
                    imageBytes,
                    "trees/" + uuid + "/decorations/" + decorationId + ".png"
            );

            // 2. Decoration 생성
            Decoration decoration = Decoration.builder()
                    .id(decorationId)
                    .authorName(request.getAuthorName())
                    .imageUrl(imageUrl)
                    .orderIndex(tree.getDecorations().size())
                    .createdAt(LocalDateTime.now())
                    .build();

            // 3. Tree 상태 변경
            tree.getDecorations().add(decoration);
            tree.touch();

            saveToRedis(tree);

            return new DecorationCreateResponse(
                    decorationId,
                    decoration.getOrderIndex()
            );

        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "장식 추가 중 오류가 발생했습니다."
            );
        }
    }

    /* =========================
       Redis 유틸 메서드
       ========================= */
    private Tree getTreeFromRedis(String uuid) {
        String key = TREE_KEY_PREFIX + uuid;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new GeneralException(ErrorStatus.TREE_NOT_FOUND);
        }

        try {
            return objectMapper.readValue(json, Tree.class);
        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "트리 데이터 파싱 중 오류가 발생했습니다."
            );
        }
    }

    private void saveToRedis(Tree tree) {
        try {
            redisTemplate.opsForValue().set(
                    TREE_KEY_PREFIX + tree.getUuid(),
                    objectMapper.writeValueAsString(tree),
                    TREE_TTL_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "트리 데이터 저장 중 오류가 발생했습니다."
            );
        }
    }

    public void reorderDecorations(String uuid, DecorationReorderRequest request) {
        Tree tree = getTreeFromRedis(uuid);

        List<Decoration> current = tree.getDecorations();
        List<String> newOrder = request.getOrder();

        if (current.size() != newOrder.size()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST, "장식 개수가 일치하지 않습니다.");
        }

        // id → Decoration 매핑
        var map = current.stream()
                .collect(Collectors.toMap(Decoration::getId, d -> d));

        List<Decoration> reordered = new ArrayList<>();

        for (int i = 0; i < newOrder.size(); i++) {
            String id = newOrder.get(i);
            Decoration decoration = map.get(id);

            if (decoration == null) {
                throw new GeneralException(ErrorStatus.DECORATION_NOT_FOUND);
            }

            reordered.add(
                    Decoration.builder()
                            .id(decoration.getId())
                            .authorName(decoration.getAuthorName())
                            .imageUrl(decoration.getImageUrl())
                            .orderIndex(i)
                            .createdAt(decoration.getCreatedAt())
                            .build()
            );
        }

        // 상태 변경
        tree.getDecorations().clear();
        tree.getDecorations().addAll(reordered);
        tree.touch();

        saveToRedis(tree);
    }

    public void deleteDecoration(String uuid, String decorationId) {
        Tree tree = getTreeFromRedis(uuid);

        List<Decoration> decorations = tree.getDecorations();

        Decoration target = decorations.stream()
                .filter(d -> d.getId().equals(decorationId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorStatus.DECORATION_NOT_FOUND));

        // 제거
        decorations.remove(target);

        // orderIndex 재정렬
        for (int i = 0; i < decorations.size(); i++) {
            Decoration d = decorations.get(i);

            decorations.set(
                    i,
                    Decoration.builder()
                            .id(d.getId())
                            .authorName(d.getAuthorName())
                            .imageUrl(d.getImageUrl())
                            .orderIndex(i)
                            .createdAt(d.getCreatedAt())
                            .build()
            );
        }

        tree.touch();
        saveToRedis(tree);
    }

}

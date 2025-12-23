package com.gathertree.demo.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gathertree.demo.ai.dto.TreeEvaluationResponse;
import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import com.gathertree.demo.s3.service.S3ImageMoveService;
import com.gathertree.demo.tree.model.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AiEvaluationService {

    /* =========================
       Constants
       ========================= */
    private static final int REQUIRED_DECORATIONS = 10;
    private static final int CACHE_TTL_HOURS = 24;

    private static final String GLOBAL_LIMIT_KEY = "ai:evaluation:count";
    private static final long GLOBAL_LIMIT = 200;

    /* =========================
       Dependencies
       ========================= */
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final OpenAiEvaluationFacade openAiEvaluationFacade;
    private final S3ImageMoveService s3ImageMoveService;

    @Value("${cloud.aws.s3.public-url}")
    private String publicUrl;

    /**
     * 트리 AI 평가
     *
     * @param tree     Redis에 저장된 트리 엔티티
     * @param mode     평가 모드 (mild | spicy)
     * @param imageKey S3에 업로드된 tmp 이미지 key (eval/tmp/xxx.png)
     */
    public TreeEvaluationResponse evaluate(
            Tree tree,
            String mode,
            String imageKey
    ) {

        /* =========================
           1️⃣ 장식 개수 조건
           ========================= */
        if (tree.getDecorations().size() < REQUIRED_DECORATIONS) {
            throw new GeneralException(
                    ErrorStatus.EVALUATION_NOT_ALLOWED,
                    "장식이 10개 모두 모였을 때만 평가할 수 있습니다."
            );
        }

        /* =========================
           2️⃣ mode 검증
           ========================= */
        if (!"mild".equals(mode) && !"spicy".equals(mode)) {
            throw new GeneralException(
                    ErrorStatus.BAD_REQUEST,
                    "mode는 mild 또는 spicy여야 합니다."
            );
        }

        /* =========================
           3️⃣ imageKey 검증
           ========================= */
        if (imageKey == null || imageKey.isBlank()) {
            throw new GeneralException(
                    ErrorStatus.BAD_REQUEST,
                    "평가할 이미지 key는 필수입니다."
            );
        }

        if (!imageKey.startsWith("eval/tmp/")) {
            throw new GeneralException(
                    ErrorStatus.BAD_REQUEST,
                    "평가 이미지는 eval/tmp 경로여야 합니다."
            );
        }

        /* =========================
           4️⃣ 캐시 확인
           ========================= */
        String cacheKey = buildCacheKey(tree.getUuid(), mode);
        TreeEvaluationResponse cached = getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        /* =========================
           5️⃣ 선착순 200회 제한
           ========================= */
        consumeGlobalLimit();

        TreeEvaluationResponse result;
        try {
            /* =========================
               6️⃣ tmp → public 이동
               ========================= */
            String publicKey = s3ImageMoveService.moveTmpToPublic(imageKey);
            String imageUrl = publicUrl + "/" + publicKey;

            /* =========================
               7️⃣ OpenAI 평가 (public URL)
               ========================= */
            result = openAiEvaluationFacade.evaluate(
                    imageUrl,
                    mode
            );

            /* =========================
               8️⃣ imageUrl 포함 응답 구성
               ========================= */
            result = TreeEvaluationResponse.builder()
                    .score(result.getScore())
                    .title(result.getTitle())
                    .summary(result.getSummary())
                    .comments(result.getComments())
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            rollbackGlobalLimit();
            throw e;
        }

        /* =========================
           9️⃣ 캐싱
           ========================= */
        putCache(cacheKey, result);

        return result;
    }

    /* =========================
       Cache Key
       ========================= */
    private String buildCacheKey(String uuid, String mode) {
        return "tree:" + uuid + ":evaluation:" + mode;
    }

    /* =========================
       Cache 조회
       ========================= */
    private TreeEvaluationResponse getCached(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, TreeEvaluationResponse.class);
        } catch (Exception e) {
            redisTemplate.delete(key);
            return null;
        }
    }

    /* =========================
       Cache 저장
       ========================= */
    private void putCache(String key, TreeEvaluationResponse result) {
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(result),
                    CACHE_TTL_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception ignored) {
        }
    }

    /* =========================
       Global Limit
       ========================= */
    private void consumeGlobalLimit() {
        Long count = redisTemplate.opsForValue().increment(GLOBAL_LIMIT_KEY);
        if (count != null && count > GLOBAL_LIMIT) {
            redisTemplate.opsForValue().decrement(GLOBAL_LIMIT_KEY);
            throw new GeneralException(
                    ErrorStatus.EVALUATION_NOT_ALLOWED,
                    "AI 평가는 선착순 200명까지만 제공됩니다."
            );
        }
    }

    private void rollbackGlobalLimit() {
        redisTemplate.opsForValue().decrement(GLOBAL_LIMIT_KEY);
    }
}

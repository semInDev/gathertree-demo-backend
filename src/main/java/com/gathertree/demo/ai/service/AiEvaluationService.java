package com.gathertree.demo.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gathertree.demo.ai.dto.TreeEvaluationResponse;
import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import com.gathertree.demo.tree.model.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AiEvaluationService {

    private static final int REQUIRED_DECORATIONS = 10;
    private static final int CACHE_TTL_HOURS = 24;

    private static final String GLOBAL_LIMIT_KEY = "ai:evaluation:count";
    private static final long GLOBAL_LIMIT = 200;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final OpenAiEvaluationFacade openAiEvaluationFacade;

    /**
     * 트리 AI 평가
     *
     * @param tree     Redis에 저장된 트리 엔티티
     * @param mode     평가 모드 (mild | spicy)
     * @param imageUrl 프론트엔드에서 합성한 최종 트리 이미지 URL
     */
    public TreeEvaluationResponse evaluate(
            Tree tree,
            String mode,
            String imageUrl
    ) {

        // 1️⃣ 장식 개수 조건
        if (tree.getDecorations().size() < REQUIRED_DECORATIONS) {
            throw new GeneralException(
                    ErrorStatus.EVALUATION_NOT_ALLOWED,
                    "장식이 10개 모두 모였을 때만 평가할 수 있습니다."
            );
        }

        // 2️⃣ mode 검증
        if (!"mild".equals(mode) && !"spicy".equals(mode)) {
            throw new GeneralException(
                    ErrorStatus.BAD_REQUEST,
                    "mode는 mild 또는 spicy여야 합니다."
            );
        }

        // 3️⃣ imageUrl 검증 (방어 코드)
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new GeneralException(
                    ErrorStatus.BAD_REQUEST,
                    "평가할 이미지 URL은 필수입니다."
            );
        }

        // 4️⃣ 캐시 먼저 확인
        String cacheKey = buildCacheKey(tree.getUuid(), mode);
        TreeEvaluationResponse cached = getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 5️⃣ 선착순 200회 제한
        consumeGlobalLimit();

        // 6️⃣ OpenAI 호출 (프론트 합성 이미지 기준)
        TreeEvaluationResponse result;
        try {
            result = openAiEvaluationFacade.evaluate(
                    imageUrl,
                    mode
            );
        } catch (Exception e) {
            rollbackGlobalLimit();
            throw e;
        }

        // 7️⃣ 캐싱
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
            // 캐시가 깨졌으면 삭제 후 무시
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
            // 캐싱 실패는 치명적이지 않으므로 무시
        }
    }

    /* =========================
       Global Limit (선착순 200)
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

package com.gathertree.demo.ai.service;

import com.gathertree.demo.ai.dto.TreeEvaluationResponse;

public interface OpenAiEvaluationFacade {
    TreeEvaluationResponse evaluate(String imageUrl, String mode);
}

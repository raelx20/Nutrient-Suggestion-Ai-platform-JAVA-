package com.vitaledge.ai;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over the LLM provider. The source project used Celery tasks calling an
 * external LLM; here the provider is swappable (real implementation + rule-based fallback).
 */
public interface LlmClient {

    String complete(String systemPrompt, List<ChatTurn> history, Map<String, Object> params);

    boolean isAvailable();

    record ChatTurn(String role, String content) {
    }
}
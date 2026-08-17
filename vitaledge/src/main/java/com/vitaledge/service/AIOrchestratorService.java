package com.vitaledge.service;

import com.vitaledge.ai.LlmClient;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.conversation.Message;
import com.vitaledge.domain.conversation.SessionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AI conversation orchestration. Ports the source project's {@code ai_orchestrator.py}
 * state machine (welcome, general, assessment, review, recommendation, post_recommendation).
 * The prompt is built from a safe, summarized health context so raw personal data never
 * reaches the model verbatim.
 */
@Service
public class AIOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AIOrchestratorService.class);
    private static final int MAX_HISTORY_TURNS = 20;
    private static final int MAX_CONTEXT_CHARS = 2000;

    private final LlmClient llmClient;

    public AIOrchestratorService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * Produce an assistant reply for the given session state.
     */
    public String handle(SessionType sessionType, String userMessage, List<Message> history,
                         HealthProfile healthProfile, List<Map<String, Object>> recommendations) {
        List<LlmClient.ChatTurn> turns = toTurns(history, userMessage);
        String context = buildSafeContext(sessionType, healthProfile, recommendations);
        String systemPrompt = buildSystemPrompt(sessionType, context);
        Map<String, Object> params = Map.of(
                "max_tokens", 600,
                "temperature", 0.7);

        if (!llmClient.isAvailable()) {
            log.debug("LLM unavailable; using fallback responses");
        }
        return llmClient.complete(systemPrompt, turns, params);
    }

    public int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }

    /** Summarize an over-long history into a compact context (defensive; safe with fallback LLM). */
    public List<LlmClient.ChatTurn> trimHistory(List<LlmClient.ChatTurn> history) {
        if (history.size() <= MAX_HISTORY_TURNS) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - MAX_HISTORY_TURNS, history.size()));
    }

    private String buildSystemPrompt(SessionType sessionType, String context) {
        String directive = switch (sessionType) {
            case welcome -> "Warm welcome. Introduce what you can help with and prompt to start an assessment.";
            case assessment -> "Guide the user through a structured health assessment. Ask one clear question at a time.";
            case review -> "Review the user's assessment results and explain scores and risk factors in plain language.";
            case recommendation -> "Recommend nutrition products aligned to the health profile. Explain why each fits.";
            case post_recommendation -> "Help the user after recommendations: usage guidance, alternatives, clarifications.";
            case general -> "General nutrition guidance. Be safe, factual and avoid medical diagnoses.";
        };
        return "You are a nutrition and wellness assistant (VitalEdge). Be concise, supportive and safe. "
                + "Never give medical diagnoses; recommend consulting a professional for serious concerns. "
                + "SESSION=" + sessionType.name() + ". " + directive + "\nCONTEXT:\n" + context;
    }

    private String buildSafeContext(SessionType sessionType, HealthProfile healthProfile,
                                    List<Map<String, Object>> recommendations) {
        StringBuilder sb = new StringBuilder();
        if (healthProfile != null) {
            Map<String, Object> data = healthProfile.getProfileData();
            Map<String, Object> scores = safeMap(data.get("scores"));
            sb.append("Health profile: overall score ").append(scores.get("overall"));
            sb.append(", bmi classification ").append(safeMap(data.get("bmi")).get("classification"));
            Object risks = data.get("risk_factors");
            sb.append(", risk factors ").append(risks == null ? "none" : risks).append(".");
            sb.append(" Counsellor recommended: ").append(healthProfile.isCounsellorRecommended()).append(". ");
        } else {
            sb.append("No completed assessment yet. ");
        }
        if (recommendations != null && !recommendations.isEmpty()) {
            sb.append(recommendations.size()).append(" tailored recommendations available. ");
        }
        String context = sb.toString();
        return context.length() > MAX_CONTEXT_CHARS ? context.substring(0, MAX_CONTEXT_CHARS) : context;
    }

    private List<LlmClient.ChatTurn> toTurns(List<Message> history, String userMessage) {
        List<LlmClient.ChatTurn> turns = new ArrayList<>();
        for (Message m : history) {
            turns.add(new LlmClient.ChatTurn(m.getRole().name(), m.getContent()));
        }
        turns.add(new LlmClient.ChatTurn("user", userMessage));
        return trimHistory(turns);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> safeMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
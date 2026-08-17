package com.vitaledge.ai;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deterministic, offline fallback that answers with safe guidance. It is used when no
 * external LLM is configured, so the platform remains fully functional and testable.
 * A production deployment swaps this bean for a real provider.
 */
@Component
public class PlaceholderLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(PlaceholderLlmClient.class);

    @Override
    public String complete(String systemPrompt, List<ChatTurn> history, Map<String, Object> params) {
        String last = history.isEmpty() ? "" : history.get(history.size() - 1).content();
        String lc = last.toLowerCase(Locale.ROOT);

        if (systemPrompt != null && systemPrompt.contains("SESSION=assessment")) {
            return "Let's build your nutrition profile. I'll walk you through a short assessment. "
                    + "Start the assessment from the menu, or type 'start assessment' to begin.";
        }
        if (systemPrompt != null && systemPrompt.contains("SESSION=recommendation")) {
            return "Based on your health profile, here are some tailored product recommendations. "
                    + "Would you like to see them, or would you prefer to review your assessment first?";
        }
        if (lc.contains("hello") || lc.contains("hi ") || lc.matches("^hi$") || lc.contains("hey")) {
            return "Hello! I'm your AI nutrition assistant. I can help with health assessments, "
                    + "nutritional guidance and product recommendations. What would you like to do?";
        }
        if (lc.contains("assessment")) {
            return "I can guide you through a personalized health assessment. Would you like to "
                    + "start now? (You can also start it from the Assessments menu.)";
        }
        if (lc.contains("recommendation") || lc.contains("recommend")) {
            return "I can recommend nutrition products tailored to your health profile. "
                    + "Complete an assessment first so I can personalize the recommendations.";
        }
        if (lc.contains("bmi") || lc.contains("weight")) {
            return "Your BMI is computed from your weight and height during the assessment. "
                    + "If you'd like, complete the assessment to see your BMI classification and scores.";
        }
        log.debug("Placeholder LLM: no rule matched for input '{}'", last);
        return "Thanks for your message. I can help with health assessments, nutrition guidance "
                + "and product recommendations. Could you tell me more about what you'd like to do?";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
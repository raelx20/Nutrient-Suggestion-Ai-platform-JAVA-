package com.vitaledge;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AssessmentFlowTest extends BaseApiTest {

    @Test
    void fullAssessmentLifecycle() throws Exception {
        String email = newVerifiedUser();
        String token = loginAndGetAccessToken(email, "StrongPass123!");
        String auth = "Bearer " + token;

        // Start assessment
        String sessionId = mockMvc.perform(post("/api/v1/assessments/start").header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assessment.status").value("in_progress"))
                .andExpect(jsonPath("$.questions", hasSize(16)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(sessionId).path("assessment").path("id").asText();

        // Submit answers
        String answers = """
                {"answers":[
                  {"questionId":"31000000-0000-0000-0000-000000000001","value":"adults"},
                  {"questionId":"31000000-0000-0000-0000-000000000002","value":"female"},
                  {"questionId":"31000000-0000-0000-0000-000000000003","value":70},
                  {"questionId":"31000000-0000-0000-0000-000000000004","value":170},
                  {"questionId":"31000000-0000-0000-0000-000000000005","value":"moderate"},
                  {"questionId":"31000000-0000-0000-0000-000000000006","value":["weight_loss","general_health"]},
                  {"questionId":"31000000-0000-0000-0000-000000000007","value":["balanced"]},
                  {"questionId":"31000000-0000-0000-0000-000000000008","value":["none"]},
                  {"questionId":"31000000-0000-0000-0000-000000000009","value":["none"]},
                  {"questionId":"31000000-0000-0000-0000-000000000010","value":false},
                  {"questionId":"31000000-0000-0000-0000-000000000011","value":["none"]},
                  {"questionId":"31000000-0000-0000-0000-000000000012","value":"O+"},
                  {"questionId":"31000000-0000-0000-0000-000000000013","value":7},
                  {"questionId":"31000000-0000-0000-0000-000000000014","value":"moderate"},
                  {"questionId":"31000000-0000-0000-0000-000000000015","value":false},
                  {"questionId":"31000000-0000-0000-0000-000000000016","value":false}
                ]}
                """;

        mockMvc.perform(post("/api/v1/assessments/{id}/answers", id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers", hasSize(16)))
                .andExpect(jsonPath("$.assessment.answeredQuestions").value(16));

        // Complete
        mockMvc.perform(post("/api/v1/assessments/{id}/complete", id)
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.healthProfile.bmi", greaterThan(0.0)))
                .andExpect(jsonPath("$.healthProfile.bmiClassification").value("normal"))
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))));

        // Retrieve
        mockMvc.perform(get("/api/v1/assessments/{id}", id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthProfile").exists())
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))));
    }

    @Test
    void cannotAccessAnotherUsersAssessment() throws Exception {
        String email = newVerifiedUser();
        String token = loginAndGetAccessToken(email, "StrongPass123!");
        String auth = "Bearer " + token;

        String sessionId = mockMvc.perform(post("/api/v1/assessments/start").header("Authorization", auth))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(sessionId).path("assessment").path("id").asText();

        String otherEmail = newVerifiedUser();
        String otherToken = loginAndGetAccessToken(otherEmail, "StrongPass123!");

        mockMvc.perform(get("/api/v1/assessments/{id}", id).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
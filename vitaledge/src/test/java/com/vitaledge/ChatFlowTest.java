package com.vitaledge;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.emptyOrNullString;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class ChatFlowTest extends BaseApiTest {

    @Test
    void sendsMessageAndReceivesReply() throws Exception {
        String email = newVerifiedUser();
        String token = loginAndGetAccessToken(email, "StrongPass123!");
        String auth = "Bearer " + token;

        MvcResult result = mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello, I want to lose weight","sessionType":"welcome"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.assistantMessage.content", not(emptyOrNullString())))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String conversationId = json.path("conversationId").asText();

        mockMvc.perform(get("/api/v1/chat/conversations/{id}", conversationId)
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(2)))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[1].role").value("assistant"));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        String email = newVerifiedUser();
        String token = loginAndGetAccessToken(email, "StrongPass123!");

        mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   ","sessionType":"welcome"}
                                """))
                .andExpect(status().is4xxClientError());
    }
}
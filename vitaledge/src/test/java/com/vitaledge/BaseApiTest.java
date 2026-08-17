package com.vitaledge;

import com.vitaledge.config.TestRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for API-level integration tests running against H2 + in-memory Redis.
 * Provides helpers to register, verify and authenticate a fresh consumer.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestRedisConfig.class)
public abstract class BaseApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MvcResult register(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"Test User"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    protected String registerAndGetVerificationToken(String email, String password) throws Exception {
        MvcResult result = register(email, password);
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("verification_token").asText();
    }

    protected void verifyEmail(String token) throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());
    }

    protected String loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("accessToken").asText();
    }

    protected String loginAndGetRefreshToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("refreshToken").asText();
    }

    protected String newVerifiedUser() throws Exception {
        String email = "user-" + System.nanoTime() + "@example.com";
        String password = "StrongPass123!";
        String token = registerAndGetVerificationToken(email, password);
        verifyEmail(token);
        return email;
    }
}
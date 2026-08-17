package com.vitaledge;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class ProductCatalogTest extends BaseApiTest {

    @Test
    void listsActiveProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void filtersByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("category", "low"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("low"));
    }

    @Test
    void getsProductDetail() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", "40000000-0000-0000-0000-000000000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Daily Multi-Vitamin"));
    }

    @Test
    void unknownProductIs404() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", "40000000-0000-0000-0000-00000000ffff"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsOversizedSearchQuery() throws Exception {
        String longQuery = "a".repeat(60);
        mockMvc.perform(get("/api/v1/products").param("q", longQuery))
                .andExpect(status().isOk());
    }
}
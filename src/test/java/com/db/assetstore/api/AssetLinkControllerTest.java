package com.db.assetstore.api;

import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssetLinkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AssetLinkRepo assetLinkRepo;

    @AfterEach
    void tearDown() {
        assetLinkRepo.deleteAll();
    }

    @Test
    void listAssetLinks_assetNotFound_returns404() throws Exception {
        mockMvc.perform(get("/assets/missing-asset/links"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAssetLinks_noLinks_returnsEmptyArray() throws Exception {
        String assetId = createAsset();

        mockMvc.perform(get("/assets/" + assetId + "/links")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listAssetLinks_includeInactiveFiltersResults() throws Exception {
        String assetId = createAsset();

        assetLinkRepo.save(AssetLinkEntity.builder()
                .assetId(assetId)
                .entityType("EXT")
                .entitySubtype("TYPE")
                .targetCode("inactive-code")
                .active(false)
                .deactivatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .deactivatedBy("tester")
                .build());

        assetLinkRepo.save(AssetLinkEntity.builder()
                .assetId(assetId)
                .entityType("EXT")
                .entitySubtype("TYPE")
                .targetCode("active-code")
                .active(true)
                .createdAt(Instant.parse("2024-01-02T00:00:00Z"))
                .createdBy("tester")
                .build());

        mockMvc.perform(get("/assets/" + assetId + "/links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetCode", is("active-code")))
                .andExpect(jsonPath("$[0].active", is(true)));

        mockMvc.perform(get("/assets/" + assetId + "/links")
                        .param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].targetCode", is("active-code")))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].targetCode", is("inactive-code")))
                .andExpect(jsonPath("$[1].active", is(false)));
    }

    private String createAsset() throws Exception {
        String payload = """
                {
                    \"type\": \"CRE\",
                    \"executedBy\": \"tester\",
                    \"attributes\": {
                        \"city\": \"Berlin\"
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString();
    }
}

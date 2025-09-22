package com.db.assetstore.web;

import com.db.assetstore.domain.service.AssetService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssetController.class)
class AssetControllerErrorTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AssetService assetService;

    @Test
    void returnsStandardizedBadRequestOnIllegalArgument() throws Exception {
        Mockito.when(assetService.addAssetFromJson(any())).thenThrow(new IllegalArgumentException("Invalid JSON payload: missing type"));

        mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid JSON payload")))
                .andExpect(jsonPath("$.path").value("/assets"));
    }
}

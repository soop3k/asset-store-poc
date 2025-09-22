package com.db.assetstore.web;

import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssetController.class)
class AssetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AssetService assetService;

    @Test
    void addAsset_returnsId() throws Exception {
        Mockito.when(assetService.addAssetFromJson(any())).thenReturn("abc-123");

        mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CRE\",\"attributes\":{}}"))
                .andExpect(status().isOk())
                .andExpect(content().string("abc-123"));
    }

    @Test
    void listAssets_returnsArray() throws Exception {
        Mockito.when(assetService.search(any(SearchCriteria.class))).thenReturn(List.of());

        mockMvc.perform(get("/assets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}

package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@AutoConfigureMockMvc
class AssetEventE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createAsset_fetch_it_and_fetch_event() throws Exception {
        String payload = "{" +
                "\"type\":\"CRE\"," +
                "\"currency\":\"USD\"," +
                "\"notionalAmount\":123.45," +
                "\"status\":\"ACTIVE\"," +
                "\"subtype\":\"OFFICE\"," +
                "\"description\":\"Test CRE\"," +
                "\"attributes\": {\"city\":\"Warsaw\", \"rooms\":3}" +
                "}";

        // create
        MvcResult res = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        String id = res.getResponse().getContentAsString();

        // fetch single asset
        mockMvc.perform(get("/assets/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.notionalAmount", is(closeTo(123.45, 0.0001))))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.subtype", is("OFFICE")))
                .andExpect(jsonPath("$.description", is("Test CRE")))
                .andExpect(jsonPath("$.attributes.city.value", is("Warsaw")))
                .andExpect(jsonPath("$.attributes.rooms.value", is(3)));

        // fetch event using new endpoint and verify transformation
        mockMvc.perform(get("/events/" + id + "/AssetCRE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.notional_amount", is(closeTo(123.45, 0.0001))))
                .andExpect(jsonPath("$.asset_status", is("ACTIVE")))
                .andExpect(jsonPath("$.property_type", is("OFFICE")));
    }
}

package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@AutoConfigureMockMvc
class AssetUpdateTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createAssetAndUpdateFields() throws Exception {
        String payload = """
                {
                    "type":"CRE",
                    "currency":"USD",
                    "notionalAmount":123.45,
                    "status":"ACTIVE",
                    "subtype":"OFFICE",
                    "description":"Original CRE",
                    "attributes": {
                        "city":"Gdansk",
                        "rooms":5
                    }
                }""";

        MvcResult res = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        String id = res.getResponse().getContentAsString();

        String update = """
                {
                    "status":"INACTIVE",
                    "description":"Updated CRE",
                    "currency":"EUR",
                    "notionalAmount":200.5
                }""";

        mockMvc.perform(put("/assets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/assets/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.currency", is("EUR")))
                .andExpect(jsonPath("$.notionalAmount", is(closeTo(200.5, 0.0001))))
                .andExpect(jsonPath("$.status", is("INACTIVE")))
                .andExpect(jsonPath("$.subtype", is("OFFICE")))
                .andExpect(jsonPath("$.description", is("Updated CRE")))
                .andExpect(jsonPath("$.attributes.city.value", is("Gdansk")))
                .andExpect(jsonPath("$.attributes.rooms.value", is(5)));
    }
}

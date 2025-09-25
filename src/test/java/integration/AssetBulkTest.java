package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@AutoConfigureMockMvc
class AssetBulkTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void bulkCreateAndFetch_creType_requestDtoArray() throws Exception {
        String payload = """
                [
                {"id":"cre-1","type":"CRE","attributes":{"city":"Warsaw","area":100.5,"rooms":3,"active":true}},
                {"id":"cre-2","type":"CRE","attributes":{"city":"Gdansk","rooms":2}}
                ]""";

        mockMvc.perform(post("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("cre-1")))
                .andExpect(jsonPath("$[1]", is("cre-2")));

        mockMvc.perform(get("/assets").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))

                .andExpect(jsonPath("$[?(@.id=='cre-1')].type", contains("CRE")))
                .andExpect(jsonPath("$[?(@.id=='cre-1')].attributes.city.value", contains("Warsaw")))
                .andExpect(jsonPath("$[?(@.id=='cre-1')].attributes.rooms.value", contains(3)))
                .andExpect(jsonPath("$[?(@.id=='cre-1')].attributes.active.value", contains(true)))

                .andExpect(jsonPath("$[?(@.id=='cre-2')].type", contains("CRE")))
                .andExpect(jsonPath("$[?(@.id=='cre-2')].attributes.city.value", contains("Gdansk")))
                .andExpect(jsonPath("$[?(@.id=='cre-2')].attributes.rooms.value", contains(2)));
    }
}

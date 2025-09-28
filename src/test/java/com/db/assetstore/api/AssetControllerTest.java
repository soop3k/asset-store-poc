package com.db.assetstore.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createSingleAsset() throws Exception {
        String payload = """
                {
                    "type": "CRE",
                    "status": "ACTIVE",
                    "subtype": "OFFICE",
                    "notionalAmount": 1500000.75,
                    "year": 2023,
                    "description": "Premium office building",
                    "currency": "USD",
                    "executedBy": "tester",
                    "attributes": {
                        "city": "New York",
                        "rooms": 25,
                        "area": 5000.5,
                        "active": true
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())))
                .andReturn();

        String assetId = result.getResponse().getContentAsString();

        mockMvc.perform(get("/assets/" + assetId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(assetId)))
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.subtype", is("OFFICE")))
                .andExpect(jsonPath("$.notionalAmount", is(closeTo(1500000.75, 0.01))))
                .andExpect(jsonPath("$.year", is(2023)))
                .andExpect(jsonPath("$.description", is("Premium office building")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.attributes.city", is("New York")))
                .andExpect(jsonPath("$.attributes.rooms", is(25)))
                .andExpect(jsonPath("$.attributes.area", is(closeTo(5000.5, 0.01))))
                .andExpect(jsonPath("$.attributes.active", is(true)));
    }

    @Test
    void createSingleAssetWithMinimalFields() throws Exception {
        String payload = """
                {
                    "type": "SHIP",
                    "executedBy": "tester",
                    "attributes": {
                        "name": "Cargo Vessel",
                        "imo": 1234567
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String assetId = result.getResponse().getContentAsString();

        mockMvc.perform(get("/assets/" + assetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("SHIP")))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.subtype").doesNotExist())
                .andExpect(jsonPath("$.attributes.name", is("Cargo Vessel")))
                .andExpect(jsonPath("$.attributes.imo", is(1234567)));
    }

    @Test
    void createSingleAssetWithProvidedId() throws Exception {
        String payload = """
                {
                    "id": "custom-asset-123",
                    "type": "CRE",
                    "executedBy": "tester",
                    "attributes": {
                        "city": "London"
                    }
                }
                """;

        mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("custom-asset-123"));

        mockMvc.perform(get("/assets/custom-asset-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("custom-asset-123")));
    }

    @Test
    void createSingleAssetWithInvalidJson() throws Exception {
        String invalidPayload = """
                {
                    "type": "CRE",
                    "invalidField": true,
                    "attributes": {
                        "city": "Berlin"
                    }
                """;

        mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBulkAssets() throws Exception {
        String payload = """
                [
                    {
                        "id": "bulk-cre-1",
                        "type": "CRE",
                        "status": "ACTIVE",
                        "executedBy": "tester",
                        "attributes": {
                            "city": "Paris",
                            "rooms": 10
                        }
                    },
                    {
                        "id": "bulk-ship-1",
                        "type": "SHIP",
                        "status": "OPERATIONAL",
                        "executedBy": "tester",
                        "attributes": {
                            "name": "Ocean Liner",
                            "imo": 7654321
                        }
                    }
                ]
                """;

        mockMvc.perform(post("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("bulk-cre-1")))
                .andExpect(jsonPath("$[1]", is("bulk-ship-1")));

        mockMvc.perform(get("/assets/bulk-cre-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.attributes.city", is("Paris")));

        mockMvc.perform(get("/assets/bulk-ship-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("SHIP")))
                .andExpect(jsonPath("$.attributes.name", is("Ocean Liner")));
    }

    @Test
    void createBulkAssetsEmptyArray() throws Exception {
        mockMvc.perform(post("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listAssetsReturnsAllWithProperStructure() throws Exception {
        String cre = """
                {
                    "id": "list-cre-1",
                    "type": "CRE",
                    "executedBy": "tester",
                    "attributes": {"city": "Berlin", "rooms": 5}
                }
                """;
        String ship = """
                {
                    "id": "list-ship-1",
                    "type": "SHIP",
                    "executedBy": "tester",
                    "attributes": {"name": "Tanker", "imo": 9999999}
                }
                """;

        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(cre))
                .andExpect(status().isOk());
        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(ship))
                .andExpect(status().isOk());

        mockMvc.perform(get("/assets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[?(@.id=='list-cre-1')].type", contains("CRE")))
                .andExpect(jsonPath("$[?(@.id=='list-cre-1')].attributes.city", contains("Berlin")))
                .andExpect(jsonPath("$[?(@.id=='list-ship-1')].type", contains("SHIP")))
                .andExpect(jsonPath("$[?(@.id=='list-ship-1')].attributes.name", contains("Tanker")));
    }

    @Test
    void listAssetsReturnsJsonArray() throws Exception {
        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getAssetReturnsFullAssetData() throws Exception {
        String payload = """
                {
                    "id": "get-test-1",
                    "type": "CRE",
                    "status": "PENDING",
                    "currency": "EUR",
                    "notionalAmount": 750000,
                    "executedBy": "tester",
                    "attributes": {
                        "city": "Amsterdam",
                        "rooms": 15,
                        "active": false
                    }
                }
                """;

        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/assets/get-test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("get-test-1")))
                .andExpect(jsonPath("$.type", is("CRE")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.currency", is("EUR")))
                .andExpect(jsonPath("$.notionalAmount", is(750000)))
                .andExpect(jsonPath("$.attributes.city", is("Amsterdam")))
                .andExpect(jsonPath("$.attributes.rooms", is(15)))
                .andExpect(jsonPath("$.attributes.active", is(false)));
    }

    @Test
    void getAssetNonExistentId() throws Exception {
        mockMvc.perform(get("/assets/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAssetupdatesAllFields() throws Exception {
        String initialPayload = """
                {
                    "id": "put-test-1",
                    "type": "CRE",
                    "status": "DRAFT",
                    "currency": "USD",
                    "executedBy": "tester",
                    "attributes": {
                        "city": "Chicago",
                        "rooms": 20
                    }
                }
                """;

        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(initialPayload))
                .andExpect(status().isOk());

        String updatePayload = """
                {
                    "status": "ACTIVE",
                    "currency": "EUR",
                    "description": "Updated description",
                    "executedBy": "updater",
                    "attributes": {
                        "city": "Brussels",
                        "rooms": 25,
                        "area": 2500.0
                    }
                }
                """;

        mockMvc.perform(put("/assets/put-test-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/assets/put-test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.currency", is("EUR")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.attributes.city", is("Brussels")))
                .andExpect(jsonPath("$.attributes.rooms", is(25)))
                .andExpect(jsonPath("$.attributes.area", is(2500)));
    }

    @Test
    void updateAssetNonExistentId() throws Exception {
        String updatePayload = """
                {
                    "status": "ACTIVE",
                    "executedBy": "updater"
                }
                """;

        mockMvc.perform(put("/assets/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchAssetUpdatesOnlyProvidedFields() throws Exception {
        String initialPayload = """
                {
                    "id": "patch-test-1",
                    "type": "CRE",
                    "status": "DRAFT",
                    "currency": "USD",
                    "description": "Original description",
                    "executedBy": "tester",
                    "attributes": {
                        "city": "Toronto",
                        "rooms": 12,
                        "active": true
                    }
                }
                """;

        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(initialPayload))
                .andExpect(status().isOk());

        String patchPayload = """
                {
                    "status": "ACTIVE",
                    "executedBy": "updater",
                    "attributes": {
                        "rooms": 18
                    }
                }
                """;

        mockMvc.perform(patch("/assets/patch-test-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/assets/patch-test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.description", is("Original description")))
                .andExpect(jsonPath("$.attributes.city", is("Toronto")))
                .andExpect(jsonPath("$.attributes.rooms", is(18)))
                .andExpect(jsonPath("$.attributes.active", is(true)));
    }

    @Test
    void patchAssetNonExistentId() throws Exception {
        mockMvc.perform(patch("/assets/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\",\"executedBy\":\"updater\"}"))
                .andExpect(status().isNotFound());
    }


    @Test
    void updatesMultipleAssetsPartially() throws Exception {
        String asset1 = """
                {
                    "id": "bulk-patch-1",
                    "type": "CRE",
                    "status": "DRAFT",
                    "executedBy": "tester",
                    "attributes": {"city": "Vienna", "rooms": 8}
                }
                """;
        String asset2 = """
                {
                    "id": "bulk-patch-2",
                    "type": "SHIP",
                    "status": "DRAFT",
                    "executedBy": "tester",
                    "attributes": {"name": "Freighter", "imo": 1111111}
                }
                """;

        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(asset1)).andExpect(status().isOk());
        mockMvc.perform(post("/assets").contentType(MediaType.APPLICATION_JSON).content(asset2)).andExpect(status().isOk());

        String bulkPatchPayload = """
                [
                    {
                        "id": "bulk-patch-1",
                        "status": "ACTIVE",
                        "executedBy": "updater",
                        "attributes": {
                            "rooms": 12
                        }
                    },
                    {
                        "id": "bulk-patch-2",
                        "status": "OPERATIONAL",
                        "executedBy": "updater",
                        "attributes": {
                            "imo": 2222222
                        }
                    }
                ]
                """;

        mockMvc.perform(patch("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPatchPayload))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/assets/bulk-patch-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.attributes.city", is("Vienna"))) // Preserved
                .andExpect(jsonPath("$.attributes.rooms", is(12))); // Updated

        mockMvc.perform(get("/assets/bulk-patch-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPERATIONAL")))
                .andExpect(jsonPath("$.attributes.name", is("Freighter"))) // Preserved
                .andExpect(jsonPath("$.attributes.imo", is(2222222))); // Updated
    }

    @Test
    void patchAssetsBulk_emptyArray_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNoContent());
    }

    @Test
    void patchAssetsBulk_withNonExistentId_returnsNotFound() throws Exception {
        String bulkPatchPayload = """
                [
                    {
                        "id": "non-existent-bulk",
                        "status": "ACTIVE",
                        "executedBy": "updater"
                    }
                ]
                """;

        mockMvc.perform(patch("/assets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPatchPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAsset_nullAttributeValues() throws Exception {
        String payload = """
                {
                    "type": "CRE",
                    "attributes": {
                        "city": "Prague",
                        "rooms": null,
                        "active": null
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String assetId = result.getResponse().getContentAsString();

        mockMvc.perform(get("/assets/" + assetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes.city", is("Prague")))
                .andExpect(jsonPath("$.attributes.rooms").doesNotExist())
                .andExpect(jsonPath("$.attributes.active").doesNotExist());
    }

    @Test
    void createAsset_mixedAttributeTypes_preservesTypeInformation() throws Exception {
        String payload = """
                {
                    "type": "CRE",
                    "status": "ACTIVE",
                    "attributes": {
                        "city": "Stockholm",
                        "rooms": 30,
                        "area": 3500.75,
                        "active": true
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String assetId = result.getResponse().getContentAsString();

        mockMvc.perform(get("/assets/" + assetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes.city", is("Stockholm")))
                .andExpect(jsonPath("$.attributes.rooms", is(30)))
                .andExpect(jsonPath("$.attributes.area", is(closeTo(3500.75, 0.01))))
                .andExpect(jsonPath("$.attributes.active", is(true)));
    }


    @Test
    void deleteAsset_marksAssetAsDeleted() throws Exception {
        String payload = """
                {
                    "type": "CRE",
                    "attributes": {
                        "city": "Lisbon"
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String assetId = result.getResponse().getContentAsString();
        String json = """
                {"id":"%s","executedBy":"tester"}
                """.formatted(assetId);
        mockMvc.perform(delete("/assets/" + assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/assets/" + assetId))
                .andExpect(status().isNotFound());
    }

}

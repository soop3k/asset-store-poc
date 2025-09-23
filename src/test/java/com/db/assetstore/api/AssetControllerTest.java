package com.db.assetstore.api;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.CreateAssetCommand;
import com.db.assetstore.domain.service.CreateAssetCommand;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.infra.api.AssetController;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.mapper.AssetRequestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@DisplayName("AssetController Data Tests")
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetQueryService assetQueryService;

    @MockBean
    private AssetCommandService commandService;

    @MockBean
    private AssetRequestMapper requestMapper;

    @BeforeEach
    void setUp() {
        // Setup minimal mocks to focus on data testing
        when(requestMapper.toCreateCommand(any(), any(), any()))
                .thenReturn(new CreateAssetCommand("test-id", AssetType.CRE, "api", null, null, null));
        when(requestMapper.toPatchCommand(any(), any(String.class), any(), any(), any()))
                .thenReturn(new CreateAssetCommand("test-id", AssetType.CRE, "api", null, null, null));
        when(requestMapper.toPatchCommand(any(), any(AssetPatchItemRequest.class), any(), any()))
                .thenReturn(new CreateAssetCommand("test-id", AssetType.CRE, "api", null, null, null));
        when(commandService.create(any())).thenReturn(new AssetId("generated-id"));
    }

    @Nested
    @DisplayName("JSON Data Validation Tests")
    class JsonDataValidationTests {

        @Test
        @DisplayName("Should accept valid asset creation JSON")
        void shouldAcceptValidAssetCreationJson() throws Exception {
            String validJson = """
                {
                    "name": "Test Asset",
                    "description": "A test asset description",
                    "type": "GENERIC",
                    "metadata": {
                        "category": "electronics",
                        "value": 1000.50
                    }
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("generated-id"));
        }

        @Test
        @DisplayName("Should reject malformed JSON")
        void shouldRejectMalformedJson() throws Exception {
            String malformedJson = """
                {
                    "name": "Test Asset",
                    "description": "Missing closing quote
                    "type": "GENERIC"
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle complex nested JSON structures")
        void shouldHandleComplexNestedJson() throws Exception {
            String complexJson = """
                {
                    "name": "Complex Asset",
                    "type": "GENERIC",
                    "attributes": {
                        "technical": {
                            "specifications": {
                                "cpu": "Intel i7",
                                "memory": "16GB",
                                "storage": "512GB SSD"
                            },
                            "warranty": {
                                "years": 3,
                                "type": "full",
                                "provider": "manufacturer"
                            }
                        },
                        "financial": {
                            "purchasePrice": 2499.99,
                            "currentValue": 1999.99,
                            "depreciationRate": 0.15
                        }
                    },
                    "tags": ["laptop", "business", "portable"],
                    "locations": [
                        {
                            "building": "HQ",
                            "floor": 3,
                            "room": "301A"
                        }
                    ]
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(complexJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle empty JSON objects")
        void shouldHandleEmptyJson() throws Exception {
            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle JSON with null values")
        void shouldHandleJsonWithNullValues() throws Exception {
            String jsonWithNulls = """
                {
                    "name": "Asset with nulls",
                    "description": null,
                    "type": "GENERIC",
                    "metadata": null,
                    "tags": null
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithNulls))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Response Data Structure Tests")
    class ResponseDataStructureTests {

        @Test
        @DisplayName("Should return properly structured asset list")
        void shouldReturnProperlyStructuredAssetList() throws Exception {
            List<Asset> mockAssets = createMockAssetList();
            when(assetQueryService.search(any(SearchCriteria.class))).thenReturn(mockAssets);

            mockMvc.perform(get("/assets")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value("asset-1"))
                    .andExpect(jsonPath("$[0].name").value("Laptop Dell"))
                    .andExpect(jsonPath("$[0].type").value("GENERIC"))
                    .andExpect(jsonPath("$[0].description").value("Business laptop"))
                    .andExpect(jsonPath("$[0].metadata.category").value("electronics"))
                    .andExpect(jsonPath("$[0].metadata.value").value(1500.0))
                    .andExpect(jsonPath("$[1].id").value("asset-2"))
                    .andExpect(jsonPath("$[1].name").value("Office Chair"))
                    .andExpect(jsonPath("$[1].type").value("GENERIC"))
                    .andExpect(jsonPath("$[1].metadata.category").value("furniture"));
        }

        @Test
        @DisplayName("Should return single asset with complete data structure")
        void shouldReturnSingleAssetWithCompleteDataStructure() throws Exception {
            Asset mockAsset = createDetailedMockAsset();
            when(assetQueryService.get(new AssetId("detailed-asset")))
                    .thenReturn(Optional.of(mockAsset));

            mockMvc.perform(get("/assets/{id}", "detailed-asset")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("detailed-asset"))
                    .andExpect(jsonPath("$.name").value("Detailed Asset"))
                    .andExpect(jsonPath("$.type").value("GENERIC"))
                    .andExpect(jsonPath("$.description").value("A very detailed asset"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andExpect(jsonPath("$.metadata").exists())
                    .andExpect(jsonPath("$.metadata.specifications").exists())
                    .andExpect(jsonPath("$.metadata.specifications.weight").value("2.5kg"))
                    .andExpect(jsonPath("$.metadata.financial").exists())
                    .andExpect(jsonPath("$.metadata.financial.purchasePrice").value(2999.99))
                    .andExpected(jsonPath("$.tags").isArray())
                    .andExpected(jsonPath("$.tags.length()").value(3))
                    .andExpected(jsonPath("$.tags[0]").value("premium"))
                    .andExpected(jsonPath("$.tags[1]").value("business"))
                    .andExpected(jsonPath("$.tags[2]").value("mobile"));
        }

        @Test
        @DisplayName("Should return empty array for no assets")
        void shouldReturnEmptyArrayForNoAssets() throws Exception {
            when(assetQueryService.search(any(SearchCriteria.class))).thenReturn(List.of());

            mockMvc.perform(get("/assets")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Bulk Operations Data Tests")
    class BulkOperationsDataTests {

        @Test
        @DisplayName("Should handle bulk creation with mixed data types")
        void shouldHandleBulkCreationWithMixedDataTypes() throws Exception {
            String bulkJson = """
                [
                    {
                        "name": "Laptop",
                        "type": "GENERIC",
                        "metadata": {
                            "price": 1299.99,
                            "quantity": 5
                        }
                    },
                    {
                        "name": "Software License",
                        "type": "GENERIC",
                        "metadata": {
                            "licenseType": "annual",
                            "users": 100,
                            "active": true
                        }
                    },
                    {
                        "name": "Building",
                        "type": "GENERIC",
                        "metadata": {
                            "address": "123 Main St",
                            "floors": 5,
                            "yearBuilt": 2020,
                            "totalArea": 25000.5
                        }
                    }
                ]
                """;

            when(commandService.create(any()))
                    .thenReturn(new AssetId("id-1"))
                    .thenReturn(new AssetId("id-2"))
                    .thenReturn(new AssetId("id-3"));

            mockMvc.perform(post("/assets/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bulkJson))
                    .andExpect(status().isOk())
                    .andExpected(jsonPath("$").isArray())
                    .andExpected(jsonPath("$.length()").value(3))
                    .andExpected(jsonPath("$[0]").value("id-1"))
                    .andExpected(jsonPath("$[1]").value("id-2"))
                    .andExpected(jsonPath("$[2]").value("id-3"));
        }

        @Test
        @DisplayName("Should handle bulk patch with different field combinations")
        void shouldHandleBulkPatchWithDifferentFieldCombinations() throws Exception {
            Asset mockAsset = createBasicMockAsset();
            when(assetQueryService.get(any(AssetId.class))).thenReturn(Optional.of(mockAsset));

            String bulkPatchJson = """
                [
                    {
                        "id": "asset-1",
                        "name": "Updated Laptop",
                        "metadata": {
                            "price": 999.99
                        }
                    },
                    {
                        "id": "asset-2",
                        "description": "Updated description only"
                    },
                    {
                        "id": "asset-3",
                        "name": "New Name",
                        "description": "New Description",
                        "metadata": {
                            "category": "updated",
                            "priority": "high",
                            "lastMaintenance": "2024-01-15"
                        }
                    }
                ]
                """;

            mockMvc.perform(patch("/assets/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bulkPatchJson))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("CRE Specific Data Tests")
    class CreSpecificDataTests {

        @Test
        @DisplayName("Should handle CRE asset with real estate specific fields")
        void shouldHandleCreAssetWithRealEstateFields() throws Exception {
            String creJson = """
                {
                    "type": "CRE",
                    "name": "Downtown Office Building",
                    "description": "Premium office space in city center",
                    "location": {
                        "address": "123 Business Ave",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10001",
                        "coordinates": {
                            "latitude": 40.7589,
                            "longitude": -73.9851
                        }
                    },
                    "specifications": {
                        "totalSquareFeet": 50000,
                        "floors": 10,
                        "yearBuilt": 2019,
                        "renovationYear": 2022,
                        "parkingSpaces": 100,
                        "elevators": 4
                    },
                    "financial": {
                        "purchasePrice": 25000000.00,
                        "currentValue": 28000000.00,
                        "annualRent": 2400000.00,
                        "operatingExpenses": 800000.00,
                        "propertyTaxes": 350000.00
                    },
                    "tenants": [
                        {
                            "name": "Tech Corp Inc",
                            "floors": [1, 2, 3],
                            "leaseStart": "2023-01-01",
                            "leaseEnd": "2028-12-31",
                            "monthlyRent": 45000.00
                        },
                        {
                            "name": "Law Firm LLC",
                            "floors": [8, 9],
                            "leaseStart": "2023-06-01",
                            "leaseEnd": "2026-05-31",
                            "monthlyRent": 28000.00
                        }
                    ],
                    "amenities": ["gym", "cafeteria", "conference_rooms", "parking_garage"],
                    "certifications": ["LEED Gold", "Energy Star"]
                }
                """;

            mockMvc.perform(post("/assets/cre")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(creJson))
                    .andExpect(status().isOk())
                    .andExpected(content().string("generated-id"));
        }

        @Test
        @DisplayName("Should handle CRE asset with minimal required fields")
        void shouldHandleCreAssetWithMinimalFields() throws Exception {
            String minimalCreJson = """
                {
                    "type": "CRE",
                    "name": "Simple Property"
                }
                """;

            mockMvc.perform(post("/assets/cre")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(minimalCreJson))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Error Response Data Tests")
    class ErrorResponseDataTests {

        @Test
        @DisplayName("Should return 404 with proper error structure for non-existent asset")
        void shouldReturn404WithProperErrorStructure() throws Exception {
            when(assetQueryService.get(new AssetId("non-existent")))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/assets/{id}", "non-existent")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for bulk patch with non-existent asset")
        void shouldReturn404ForBulkPatchWithNonExistentAsset() throws Exception {
            when(assetQueryService.get(new AssetId("non-existent")))
                    .thenReturn(Optional.empty());

            String bulkPatchJson = """
                [
                    {
                        "id": "non-existent",
                        "name": "This should fail"
                    }
                ]
                """;

            mockMvc.perform(patch("/assets/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bulkPatchJson))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Special Characters and Edge Cases")
    class SpecialCharactersAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in asset data")
        void shouldHandleSpecialCharactersInAssetData() throws Exception {
            String specialCharsJson = """
                {
                    "name": "Asset with special chars: éñüñà & símböls!",
                    "description": "Description with quotes \\\"double\\\" and 'single' and newlines\\nand tabs\\t",
                    "type": "GENERIC",
                    "metadata": {
                        "unicode": "测试中文字符",
                        "emoji": "🏢📊💻",
                        "symbols": "©®™€£¥",
                        "math": "∑∆∏√∞"
                    }
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(specialCharsJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle very large numbers and precision")
        void shouldHandleVeryLargeNumbersAndPrecision() throws Exception {
            String largeNumbersJson = """
                {
                    "name": "Asset with large numbers",
                    "type": "GENERIC",
                    "metadata": {
                        "veryLargeNumber": 999999999999999999,
                        "verySmallNumber": 0.000000000001,
                        "scientificNotation": 1.23e+15,
                        "negativeNumber": -999999.99,
                        "preciseDecimal": 123.456789012345
                    }
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(largeNumbersJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle deeply nested JSON structures")
        void shouldHandleDeeplyNestedJsonStructures() throws Exception {
            String deeplyNestedJson = """
                {
                    "name": "Deeply nested asset",
                    "type": "GENERIC",
                    "level1": {
                        "level2": {
                            "level3": {
                                "level4": {
                                    "level5": {
                                        "deepValue": "Found me!",
                                        "deepArray": [1, 2, 3, {"nested": true}]
                                    }
                                }
                            }
                        }
                    }
                }
                """;

            mockMvc.perform(post("/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(deeplyNestedJson))
                    .andExpect(status().isOk());
        }
    }

    // Helper methods to create test data
    private List<Asset> createMockAssetList() {
        Asset asset1 = new Asset();
        asset1.setId("asset-1");
        asset1.setName("Laptop Dell");
        asset1.setType(AssetType.GENERIC);
        asset1.setDescription("Business laptop");
        asset1.setCreatedAt(Instant.parse("2024-01-01T10:00:00Z"));
        asset1.setUpdatedAt(Instant.parse("2024-01-15T14:30:00Z"));

        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("category", "electronics");
        metadata1.put("value", 1500.0);
        asset1.setMetadata(metadata1);

        Asset asset2 = new Asset();
        asset2.setId("asset-2");
        asset2.setName("Office Chair");
        asset2.setType(AssetType.GENERIC);
        asset2.setDescription("Ergonomic office chair");
        asset2.setCreatedAt(Instant.parse("2024-01-02T09:00:00Z"));
        asset2.setUpdatedAt(Instant.parse("2024-01-10T16:00:00Z"));

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("category", "furniture");
        metadata2.put("value", 300.0);
        asset2.setMetadata(metadata2);

        return List.of(asset1, asset2);
    }

    private Asset createDetailedMockAsset() {
        Asset asset = new Asset();
        asset.setId("detailed-asset");
        asset.setName("Detailed Asset");
        asset.setType(AssetType.GENERIC);
        asset.setDescription("A very detailed asset");
        asset.setCreatedAt(Instant.parse("2024-01-01T10:00:00Z"));
        asset.setUpdatedAt(Instant.parse("2024-01-15T14:30:00Z"));

        Map<String, Object> metadata = new HashMap<>();

        Map<String, Object> specifications = new HashMap<>();
        specifications.put("weight", "2.5kg");
        specifications.put("dimensions", "30x20x2cm");
        specifications.put("color", "silver");
        metadata.put("specifications", specifications);

        Map<String, Object> financial = new HashMap<>();
        financial.put("purchasePrice", 2999.99);
        financial.put("currentValue", 2500.00);
        financial.put("depreciationRate", 0.1);
        metadata.put("financial", financial);

        asset.setMetadata(metadata);
        asset.setTags(List.of("premium", "business", "mobile"));

        return asset;
    }

    private Asset createBasicMockAsset() {
        Asset asset = new Asset();
        asset.setId("basic-asset");
        asset.setName("Basic Asset");
        asset.setType(AssetType.GENERIC);
        asset.setDescription("Basic test asset");
        asset.setCreatedAt(Instant.now());
        asset.setUpdatedAt(Instant.now());
        return asset;
    }
}
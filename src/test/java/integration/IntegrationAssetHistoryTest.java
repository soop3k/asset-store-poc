package integration;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetHistoryService;
import com.db.assetstore.domain.service.AssetQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrationAssetHistoryTest {

    @Autowired
    AssetCommandService commandService;
    @Autowired
    AssetQueryService queryService;
    @Autowired
    AssetHistoryService historyService;

    private final AssetJsonFactory factory = new AssetJsonFactory();

    @Test
    void multipleUpdatesProduceHistoryAndFinalState() throws Exception {
        // create CRE asset
        String id = commandService.create(factory.fromJsonForType(AssetType.CRE,
                "{\"id\":\"cre-int-hist\",\"city\":\"Gdansk\",\"rooms\":1,\"active\":true}")).id();
        assertEquals("cre-int-hist", id);

        // 1st update: change city
        applyAttributeUpdate(id, "{\"attributes\":{\"city\":\"Warsaw\"}}\n");
        // 2nd update: change rooms
        applyAttributeUpdate(id, "{\"attributes\":{\"rooms\":2}}\n");
        // 3rd update: change active
        applyAttributeUpdate(id, "{\"attributes\":{\"active\":false}}\n");

        // verify final state (defensive via flat list)
        Asset after = queryService.get(new AssetId(id)).orElseThrow();
        var flat = after.getAttributesFlat();
        var city = flat.stream().filter(av -> av instanceof AVString && "city".equals(av.name()))
                .map(av -> (AVString) av).findFirst().orElseThrow();
        var rooms = flat.stream().filter(av -> av instanceof AVDecimal && "rooms".equals(av.name()))
                .map(av -> (AVDecimal) av).findFirst().orElseThrow();
        var active = flat.stream().filter(av -> av instanceof AVBoolean && "active".equals(av.name()))
                .map(av -> (AVBoolean) av).findFirst().orElseThrow();
        assertEquals("Warsaw", city.value());
        assertEquals(new BigDecimal("2"), rooms.value());
        assertEquals(Boolean.FALSE, active.value());

        // verify history
        List<AttributeHistory> history = historyService.history(new AssetId(id));
        assertTrue(history.size() >= 3, "Expected at least 3 history entries");

        AttributeHistory last = history.get(history.size() - 1);
        assertEquals("active", last.name());
        assertEquals(Boolean.TRUE, last.valueBool());

        AttributeHistory prev = history.get(history.size() - 2);
        assertEquals("rooms", prev.name());
        assertEquals(new BigDecimal("1"), prev.valueNum());

        AttributeHistory prev2 = history.get(history.size() - 3);
        assertEquals("city", prev2.name());
        assertEquals("Gdansk", prev2.valueStr());

        assertNotNull(last.changedAt());
        assertNotNull(prev.changedAt());
        assertNotNull(prev2.changedAt());
    }

    private void applyAttributeUpdate(String id, String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        JsonNode attrs = node.get("attributes");
        Asset asset = queryService.get(new AssetId(id)).orElseThrow();
        AttributeJsonReader reader = new AttributeJsonReader(new ObjectMapper());
        List<AttributeValue<?>> avs = reader.read(asset.getType(), attrs);
        commandService.update(new AssetId(id), AssetPatch.builder().attributes(avs).build());
    }
}

package integration;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.AssetQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.db.assetstore.AssetStorePocApplication.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrationAssetPersistenceTest {

    @Autowired
    AssetCommandService commandService;
    @Autowired
    AssetQueryService queryService;
    @Autowired
    AssetJsonFactory factory;

    @Test
    void persistAndReadCreAsset() {
        String json = """
        {
            "id": "cre-int-1",
            "type": "CRE",
            "city": "Poznan",
            "rooms": 2,
            "active": true
        }
        """;
        Asset asset = factory.fromJson(json);
        var createCmd = com.db.assetstore.domain.service.cmd.CreateAssetCommand.builder()
                .id(asset.getId())
                .type(asset.getType())
                .attributes(asset.getAttributesFlat())
                .build();
        String id = commandService.create(createCmd);
        assertEquals("cre-int-1", id);

        Asset persisted = queryService.get(id).orElseThrow();
        assertEquals(AssetType.CRE, persisted.getType());
        assertEquals("cre-int-1", persisted.getId());

        // attributes (defensive lookup via flat list)
        var flat = persisted.getAttributesFlat();
        var city = flat.stream().filter(av -> av instanceof AVString && "city".equals(av.name()))
                .map(av -> (AVString) av).findFirst().orElseThrow();
        var rooms = flat.stream().filter(av -> av instanceof AVDecimal && "rooms".equals(av.name()))
                .map(av -> (AVDecimal) av).findFirst().orElseThrow();
        var active = flat.stream().filter(av -> av instanceof AVBoolean && "active".equals(av.name()))
                .map(av -> (AVBoolean) av).findFirst().orElseThrow();

        assertEquals("Poznan", city.value());
        assertEquals(new BigDecimal("2"), rooms.value());
        assertEquals(Boolean.TRUE, active.value());
    }
}

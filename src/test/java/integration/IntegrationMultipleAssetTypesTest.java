package integration;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
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
class IntegrationMultipleAssetTypesTest {

    @Autowired
    AssetCommandService commandService;
    @Autowired
    AssetQueryService queryService;
    @Autowired
    AssetJsonFactory factory;

    @Test
    void persistTwoDifferentAssetTypes_CRE_and_SHIP() {
        String creId = commandService.create(factory.fromJsonForType(AssetType.CRE,
                "{\"id\":\"cre-int-2\",\"city\":\"Lublin\"}")).id();
        assertEquals("cre-int-2", creId);

        String shipId = commandService.create(factory.fromJsonForType(AssetType.SHIP,
                "{\"id\":\"ship-int-1\",\"name\":\"Evergreen\",\"imo\":1234567}")).id();
        assertEquals("ship-int-1", shipId);

        Asset cre = queryService.get(new AssetId(creId)).orElseThrow();
        assertEquals(AssetType.CRE, cre.getType());
        var creCity = cre.getAttributesFlat().stream()
                .filter(av -> av instanceof AVString && "city".equals(av.name()))
                .map(av -> (AVString) av).findFirst().orElseThrow();
        assertEquals("Lublin", creCity.value());

        Asset ship = queryService.get(new AssetId(shipId)).orElseThrow();
        assertEquals(AssetType.SHIP, ship.getType());
        var shipName = ship.getAttributesFlat().stream()
                .filter(av -> av instanceof AVString && "name".equals(av.name()))
                .map(av -> (AVString) av).findFirst().orElseThrow();
        var shipImo = ship.getAttributesFlat().stream()
                .filter(av -> av instanceof AVDecimal && "imo".equals(av.name()))
                .map(av -> (AVDecimal) av).findFirst().orElseThrow();
        assertEquals("Evergreen", shipName.value());
        assertEquals(new BigDecimal("1234567"), shipImo.value());
    }
}

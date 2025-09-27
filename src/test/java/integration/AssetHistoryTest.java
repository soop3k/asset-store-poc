package integration;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.asset.AssetCommandService;
import com.db.assetstore.domain.service.asset.AssetHistoryService;
import com.db.assetstore.domain.service.asset.AssetQueryService;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
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
class AssetHistoryTest {

    @Autowired
    AssetCommandService commandService;
    @Autowired
    AssetQueryService queryService;
    @Autowired
    AssetHistoryService historyService;

    @Test
    void multipleUpdatesProduceHistoryAndFinalState() {
        CreateAssetCommand createCmd = CreateAssetCommand.builder()
                .id("cre-int-hist")
                .type(AssetType.CRE)
                .attributes(List.of(
                        new AVString("city", "Gdansk"),
                        new AVDecimal("rooms", new BigDecimal("1")),
                        new AVBoolean("active", true)
                ))
                .build();
        String id = commandService.create(createCmd);
        assertEquals("cre-int-hist", id);

        // 1st update: change city
        PatchAssetCommand patchCity = PatchAssetCommand.builder()
                .assetId(id)
                .attributes(List.of(new AVString("city", "Warsaw")))
                .build();
        commandService.update(patchCity);

        // 2nd update: change rooms
        PatchAssetCommand patchRooms = PatchAssetCommand.builder()
                .assetId(id)
                .attributes(List.of(new AVDecimal("rooms", new BigDecimal("2"))))
                .build();
        commandService.update(patchRooms);

        // 3rd update: change active
        PatchAssetCommand patchActive = PatchAssetCommand.builder()
                .assetId(id)
                .attributes(List.of(new AVBoolean("active", false)))
                .build();
        commandService.update(patchActive);

        Asset after = queryService.get(id).orElseThrow();
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
        List<AttributeHistory> history = historyService.history(id);
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
}

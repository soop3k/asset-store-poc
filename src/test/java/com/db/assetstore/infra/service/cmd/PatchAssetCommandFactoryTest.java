package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatchAssetCommandFactoryTest {

    @Mock
    AttributeJsonReader attributeJsonReader;

    @InjectMocks
    PatchAssetCommandFactory factory;

    private AssetPatchRequest request;
    private ObjectNode attributes;

    @BeforeEach
    void setUp() {
        attributes = new ObjectMapper().createObjectNode();
        attributes.put("rooms", 5);

        request = new AssetPatchRequest();
        request.setId("asset-2");
        request.setStatus("ACTIVE");
        request.setSubtype("OFFICE");
        request.setNotionalAmount(new BigDecimal("321.10"));
        request.setYear(2025);
        request.setDescription("Updated");
        request.setCurrency("EUR");
        request.setAttributes(attributes);
    }

    @Test
    void createCommand_buildsCommandWithAttributes() {
        AttributeValue<?> av = mock(AttributeValue.class);
        when(attributeJsonReader.read(AssetType.CRE, attributes)).thenReturn(List.of(av));

        PatchAssetCommand cmd = factory.createCommand(AssetType.CRE, "asset-2", request);

        assertThat(cmd.assetId()).isEqualTo("asset-2");
        assertThat(cmd.status()).isEqualTo("ACTIVE");
        assertThat(cmd.subtype()).isEqualTo("OFFICE");
        assertThat(cmd.notionalAmount()).isEqualTo(new BigDecimal("321.10"));
        assertThat(cmd.year()).isEqualTo(2025);
        assertThat(cmd.description()).isEqualTo("Updated");
        assertThat(cmd.currency()).isEqualTo("EUR");
        assertThat(cmd.attributes()).containsExactly(av);

        verify(attributeJsonReader).read(AssetType.CRE, attributes);
    }

    @Test
    void createCommand_withNullAttributes_usesEmptyList() {
        request.setAttributes(null);

        PatchAssetCommand cmd = factory.createCommand(AssetType.SHIP, "asset-2", request);

        assertThat(cmd.attributes()).isEmpty();
    }
}

package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
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
class CreateAssetCommandFactoryTest {

    @Mock
    AttributeJsonReader attributeJsonReader;

    @InjectMocks
    CreateAssetCommandFactory factory;

    private ObjectNode attributes;

    @BeforeEach
    void setUp() {
        attributes = new ObjectMapper().createObjectNode();
        attributes.put("city", "Berlin");
    }

    @Test
    void createCommand_populatesCommandAndParsesAttributes() {
        AttributeValue<?> av = mock(AttributeValue.class);
        when(attributeJsonReader.read(AssetType.CRE, attributes)).thenReturn(List.of(av));

        AssetCreateRequest request = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("123.45"),
                2024,
                "Desc",
                "USD",
                attributes
        );

        CreateAssetCommand cmd = factory.createCommand(request);

        assertThat(cmd.id()).isEqualTo("asset-1");
        assertThat(cmd.type()).isEqualTo(AssetType.CRE);
        assertThat(cmd.status()).isEqualTo("ACTIVE");
        assertThat(cmd.subtype()).isEqualTo("OFFICE");
        assertThat(cmd.notionalAmount()).isEqualTo(new BigDecimal("123.45"));
        assertThat(cmd.year()).isEqualTo(2024);
        assertThat(cmd.description()).isEqualTo("Desc");
        assertThat(cmd.currency()).isEqualTo("USD");
        assertThat(cmd.attributes()).containsExactly(av);

        verify(attributeJsonReader).read(AssetType.CRE, attributes);
    }

    @Test
    void createCommand_withNullAttributes_usesEmptyList() {
        AssetCreateRequest request = new AssetCreateRequest(
                null,
                AssetType.SHIP,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        CreateAssetCommand cmd = factory.createCommand(request);

        assertThat(cmd.attributes()).isEmpty();
    }
}

package com.db.assetstore.infra.json;

import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AttributesCollectionSerializerTest {

    private final ObjectMapper mapper = new JsonMapperProvider().objectMapper();

    @Test
    void serializesSingleValues() throws Exception {
        AttributesCollection attributes = AttributesCollection.empty()
                .add("city", "Warsaw")
                .add("rooms", BigDecimal.valueOf(3))
                .add("active", true);

        String json = mapper.writeValueAsString(attributes);

        assertThat(json).isEqualTo("{\"city\":\"Warsaw\",\"rooms\":3,\"active\":true}");
    }

    @Test
    void serializesMultiValuesAsArray() throws Exception {
        AttributesCollection attributes = AttributesCollection.empty()
                .add(AVString.of("tags", "A"))
                .add(AVString.of("tags", "B"))
                .add(AVDecimal.of("values", 1))
                .add(AVDecimal.of("values", 2));

        String json = mapper.writeValueAsString(attributes);

        assertThat(json).isEqualTo("{\"tags\":[\"A\",\"B\"],\"values\":[1,2]}");
    }

    @Test
    void serializesNullValues() throws Exception {
        AttributesCollection attributes = AttributesCollection.empty()
                .add(AVBoolean.of("flags", null))
                .add(AVBoolean.of("flags", true));

        String json = mapper.writeValueAsString(attributes);

        assertThat(json).isEqualTo("{\"flags\":[null,true]}");
    }
}
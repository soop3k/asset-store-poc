package com.db.assetstore.infra.config;

import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.infra.json.AttributesCollectionSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;

@Configuration
public class JsonMapperProvider {

    static class BigDecimalPlainSerializer extends StdScalarSerializer<BigDecimal> {
        protected BigDecimalPlainSerializer() { super(BigDecimal.class); }
        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            String plain = value.stripTrailingZeros().toPlainString();
            gen.writeNumber(plain);
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule serializers = new SimpleModule()
                .addSerializer(BigDecimal.class, new BigDecimalPlainSerializer())
                .addSerializer(AttributesCollection.class, new AttributesCollectionSerializer());

        JsonMapper.Builder b = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new Jdk8Module())
                .addModule(serializers)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .serializationInclusion(JsonInclude.Include.NON_NULL);

        return b.build();
    }
}

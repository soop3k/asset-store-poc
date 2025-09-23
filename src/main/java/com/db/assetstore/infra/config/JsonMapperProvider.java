package com.db.assetstore.infra.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
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
        SimpleModule numbers = new SimpleModule()
                .addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());

        JsonMapper.Builder b = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(numbers)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .serializationInclusion(JsonInclude.Include.NON_NULL);

        return b.build();
    }
}

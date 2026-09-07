package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS;
import static tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static tools.jackson.databind.cfg.EnumFeature.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
            .featuresToEnable(
                READ_ENUMS_USING_TO_STRING,
                READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE,
                WRITE_ENUMS_USING_TO_STRING,
                ACCEPT_SINGLE_VALUE_AS_ARRAY,
                ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                READ_ENUMS_USING_TO_STRING)
            .featuresToDisable(
                UNWRAP_SINGLE_VALUE_ARRAYS,
                FAIL_ON_EMPTY_BEANS,
                FAIL_ON_UNKNOWN_PROPERTIES,
                FAIL_ON_IGNORED_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        return objectMapper;
    }
}

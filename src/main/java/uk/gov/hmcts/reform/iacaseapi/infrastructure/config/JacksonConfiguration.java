package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
            .featuresToEnable(READ_ENUMS_USING_TO_STRING)
            .featuresToEnable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .featuresToEnable(WRITE_ENUMS_USING_TO_STRING)
            .serializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}

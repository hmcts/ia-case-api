package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;

@Configuration
public class JacksonConfiguration {

    // Boot 4 auto-configures a JsonMapper.Builder (and, from it, the primary JsonMapper
    // bean) itself. This customizer is applied to that shared builder, so any
    // spring.jackson.* properties still take effect alongside these settings.
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
            .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, true)
            .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .configure(EnumFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .changeDefaultPropertyInclusion(inclusion ->
                                                inclusion.withValueInclusion(JsonInclude.Include.NON_NULL));
    }

}

package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS;
import static tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static tools.jackson.databind.cfg.EnumFeature.READ_ENUMS_USING_TO_STRING;
import static tools.jackson.databind.cfg.EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;
import static tools.jackson.databind.cfg.EnumFeature.WRITE_ENUMS_USING_TO_STRING;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.json.JsonMapper;

public final class JsonMapperFactory {

    public static final JsonMapper CUSTOM_MAPPER = create();

    private JsonMapperFactory() {
    }

    public static JsonMapper create() {
        return JsonMapper.builder()
            .enable(
                READ_ENUMS_USING_TO_STRING,
                READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE,
                WRITE_ENUMS_USING_TO_STRING)
            .enable(ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .disable(
                UNWRAP_SINGLE_VALUE_ARRAYS,
                FAIL_ON_UNKNOWN_PROPERTIES,
                FAIL_ON_IGNORED_PROPERTIES,
                FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(FAIL_ON_EMPTY_BEANS)
            .changeDefaultPropertyInclusion(incl -> incl
                .withValueInclusion(JsonInclude.Include.NON_NULL)
                .withContentInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }
}

package uk.gov.hmcts.reform.bailcaseapi.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public final class MapSerializer {

    private static ObjectMapper MAPPER = new ObjectMapper();

    private MapSerializer() {
        // noop
    }

    public static void setObjectMapper(
        ObjectMapper objectMapper
    ) {
        MAPPER = objectMapper;
    }

    public static Map<String, Object> deserialize(String source) throws IOException {

        return MAPPER.readValue(
            source,
            new TypeReference<Map<String, Object>>() {
            }
        );
    }

    public static String serialize(Map<String, Object> map) throws IOException {

        return MAPPER.writeValueAsString(map);
    }
}

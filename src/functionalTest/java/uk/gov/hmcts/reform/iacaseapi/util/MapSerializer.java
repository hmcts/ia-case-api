package uk.gov.hmcts.reform.iacaseapi.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.util.Map;

public final class MapSerializer {

    private static ObjectMapper MAPPER = new JsonMapper();

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

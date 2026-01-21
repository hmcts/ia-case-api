package uk.gov.hmcts.reform.bailcaseapi.util;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class MapValueExtractor {

    private MapValueExtractor() {
        // noop
    }

    public static <T> T extract(Map<String, Object> map, String path) {

        if (!path.contains(".")) {
            return (T) map.get(path);
        }

        Map<String, Object> currentMap = map;

        String[] pathParts = path.split("\\.");

        for (int i = 0; i < pathParts.length - 1; i++) {

            Object value = currentMap.get(pathParts[i]);

            if (!(value instanceof Map)) {
                return null;
            }

            currentMap = (Map<String, Object>) value;
        }

        return (T) currentMap.get(pathParts[pathParts.length - 1]);
    }

    public static <T> T extractOrDefault(Map<String, Object> map, String path, T defaultValue) {

        T value = extract(map, path);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T extractOrThrow(Map<String, Object> map, String path) {

        T value = extract(map, path);

        if (value == null) {
            throw new RuntimeException("Missing value for path: " + path);
        }

        return value;
    }
}

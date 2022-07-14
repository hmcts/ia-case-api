package uk.gov.hmcts.reform.bailcaseapi.util;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class MapMerger {

    private MapMerger() {
        // noop
    }

    public static void merge(
        Map<String, Object> targetMap,
        Map<String, Object> sourceMap
    ) {
        for (Map.Entry<String, Object> difference : sourceMap.entrySet()) {

            String key = difference.getKey();
            Object value = difference.getValue();

            if ((targetMap.get(key) instanceof Map) && (value instanceof Map)) {

                merge(
                    (Map<String, Object>) targetMap.get(key),
                    (Map<String, Object>) value
                );

            } else {
                targetMap.put(key, value);
            }
        }
    }
}

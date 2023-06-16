package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceLoader {

    private ResourceLoader() {

    }

    public static String loadJson(final String filePath) throws Exception {
        return new String(loadResource(filePath), Charset.forName("utf-8"));
    }

    public static byte[] loadResource(final String filePath) throws Exception {
        InputStream io = null;
        byte[] allBytes = null;
        try {
            io = ResourceLoader.class.getClassLoader().getResourceAsStream(filePath);

            if (io == null) {
                throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
            } else {
                allBytes = io.readAllBytes();
            }
        } finally {
            io.close();
        }

        return allBytes;
    }

    public static <T> T loadJsonToObject(String filePath, Class<T> type) {
        try {
            return new ObjectMapper().readValue(loadJson(filePath), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String objectToJson(T object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

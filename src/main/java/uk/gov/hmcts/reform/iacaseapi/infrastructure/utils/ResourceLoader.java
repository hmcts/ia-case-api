package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceLoader {

    private ResourceLoader() {

    }

    public static String loadJson(final String filePath) throws Exception {
        return new String(loadResource(filePath), Charset.forName("utf-8"));
    }

    public static byte[] loadResource(final String filePath) throws Exception {
        byte[] allBytes = null;
        try (InputStream io = ResourceLoader.class.getClassLoader().getResourceAsStream(filePath)) {

            if (io == null) {
                throw new IllegalArgumentException(String.format("Empty resource in path %s", filePath));
            } else {
                allBytes = io.readAllBytes();
            }
        } catch (NullPointerException nullPointerException) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        return allBytes;
    }
}

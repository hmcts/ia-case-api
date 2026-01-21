package uk.gov.hmcts.reform.bailcaseapi.util;

import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceLoader {

    private ResourceLoader() {
        // Utils classes should not have public or default constructors
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
}

package uk.gov.hmcts.reform.bailcaseapi.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

public final class BinaryResourceLoader {

    private BinaryResourceLoader() {
        // noop
    }

    public static Map<String, Resource> load(String locationPattern) throws IOException {

        Resource[] resources =
            new PathMatchingResourcePatternResolver()
                .getResources(locationPattern);

        return
            Stream
                .of(resources)
                .collect(Collectors.toMap(
                    Resource::getFilename,
                    Function.identity(),
                    (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    },
                    TreeMap::new
                ));
    }

    private static String loadResourceToString(Resource r) {

        try {

            return StreamUtils.copyToString(
                r.getInputStream(),
                Charset.defaultCharset()
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

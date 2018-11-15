package uk.gov.hmcts.reform.iacaseapi.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

public final class StringResourceLoader {

    private StringResourceLoader() {
        // noop
    }

    public static Map<String, String> load(String locationPattern) throws IOException {

        Resource[] resources =
            new PathMatchingResourcePatternResolver()
                .getResources(locationPattern);

        return
            Stream
                .of(resources)
                .collect(
                    Collectors.toMap(
                        Resource::getFilename,
                        r -> {

                            try {

                                return StreamUtils.copyToString(
                                    r.getInputStream(),
                                    Charset.defaultCharset()
                                );

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                );
    }
}

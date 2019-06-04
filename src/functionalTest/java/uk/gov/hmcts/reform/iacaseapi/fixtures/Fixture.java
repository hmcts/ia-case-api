package uk.gov.hmcts.reform.iacaseapi.fixtures;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public interface Fixture {

    void prepare() throws IOException;

    default Map<String, String> getProperties() {
        return Collections.emptyMap();
    }
}

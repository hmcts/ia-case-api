package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.serialization;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization.Deserializer;

@Component
public class AsylumCaseCallbackDeserializer implements Deserializer<Callback<AsylumCase>> {

    private static final org.slf4j.Logger LOG = getLogger(AsylumCaseCallbackDeserializer.class);

    private final ObjectMapper mapper;

    public AsylumCaseCallbackDeserializer(
        @Autowired ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public Callback<AsylumCase> deserialize(
        String source
    ) {
        try {

            return mapper.readValue(
                source,
                new TypeReference<Callback<AsylumCase>>() {}
            );

        } catch (IOException e) {
            LOG.warn("Could not deserialize callback:\n{}\n{}", e.getMessage(), source);
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}

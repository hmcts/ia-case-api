package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;

@Component
public class AsylumCaseCcdEventDeserializer implements Deserializer<CcdEvent<AsylumCase>> {

    private static final org.slf4j.Logger LOG = getLogger(AsylumCaseCcdEventDeserializer.class);

    private final ObjectMapper mapper;

    public AsylumCaseCcdEventDeserializer(
        @Autowired ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public CcdEvent<AsylumCase> deserialize(
        String source
    ) {
        try {

            return mapper.readValue(
                source,
                new TypeReference<CcdEvent<AsylumCase>>() {}
            );

        } catch (IOException e) {
            LOG.warn("Could not deserialize Asylum Case Ccd Event:\n{}", source);
            throw new IllegalArgumentException("Could not deserialize Asylum Case Ccd Event", e);
        }
    }
}

package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Component
public class AsylumCaseCallbackDeserializer implements Deserializer<Callback<AsylumCase>> {

    private final ObjectMapper mapper;

    public AsylumCaseCallbackDeserializer(
        ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public Callback<AsylumCase> deserialize(
        String source
    ) {
        try {

            return mapper.readValue(
                source,
                new TypeReference<Callback<AsylumCase>>() {
                }
            );

        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}

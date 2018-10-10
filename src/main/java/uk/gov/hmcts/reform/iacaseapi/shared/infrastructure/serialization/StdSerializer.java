package uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StdSerializer<T> implements Serializer<T> {

    private static final org.slf4j.Logger LOG = getLogger(StdSerializer.class);

    private final ObjectMapper mapper;

    public StdSerializer(
        @Autowired ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public String serialize(
        T data
    ) {
        try {

            return mapper.writeValueAsString(data);

        } catch (JsonProcessingException e) {
            LOG.warn("Could not serialize data");
            throw new IllegalArgumentException("Could not serialize data", e);
        }
    }
}

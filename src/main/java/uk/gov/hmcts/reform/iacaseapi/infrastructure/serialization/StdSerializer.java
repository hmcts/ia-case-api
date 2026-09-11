package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

@Component
public class StdSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper;

    public StdSerializer(
        ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public String serialize(
        T data
    ) {
        try {

            return mapper.writeValueAsString(data);

        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not serialize data", e);
        }
    }
}

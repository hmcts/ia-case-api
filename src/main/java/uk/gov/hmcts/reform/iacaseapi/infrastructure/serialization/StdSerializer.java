package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

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

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize data", e);
        }
    }
}

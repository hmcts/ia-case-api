package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class IdValueMixin<T> {

    @JsonCreator
    public IdValueMixin(
        @JsonProperty("id") String id,
        @JsonProperty("value") T value
    ) {
    }
}
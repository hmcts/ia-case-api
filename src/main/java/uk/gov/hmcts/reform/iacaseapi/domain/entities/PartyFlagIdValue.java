package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PartyFlagIdValue {

    @JsonProperty("id")
    private String partyId;
    private StrategicCaseFlag value;

    private PartyFlagIdValue() {
        // noop -- for deserializer
    }

    public PartyFlagIdValue(
        String partyId,
        StrategicCaseFlag value
    ) {
        requireNonNull(partyId);
        requireNonNull(value);

        this.partyId = partyId;
        this.value = value;
    }

    public String getPartyId() {
        requireNonNull(partyId);
        return partyId;
    }

    public StrategicCaseFlag getValue() {
        requireNonNull(value);
        return value;
    }
}

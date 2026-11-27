package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PreviousRepresentation {

    private String legalRepCompany;
    private String legalRepReferenceNumber;

    @JsonCreator
    private PreviousRepresentation() {
        // noop -- for deserializer
    }

    public PreviousRepresentation(
        String legalRepCompany,
        String legalRepReferenceNumber
    ) {
        requireNonNull(legalRepCompany);
        requireNonNull(legalRepReferenceNumber);

        this.legalRepCompany = legalRepCompany;
        this.legalRepReferenceNumber = legalRepReferenceNumber;
    }

    public String getLegalRepCompany() {
        requireNonNull(legalRepCompany);
        return legalRepCompany;
    }

    public String getLegalRepReferenceNumber() {
        requireNonNull(legalRepReferenceNumber);
        return legalRepReferenceNumber;
    }
}

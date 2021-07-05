package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PreviousRepresentationTest {

    private final String legalRepCompany = "Some Company";
    private final String legalRepReferenceNumber = "reference-1234";

    private PreviousRepresentation previousRepresentation = new PreviousRepresentation(
        legalRepCompany,
        legalRepReferenceNumber
    );

    @Test
    void should_hold_onto_values() {
        assertEquals(legalRepCompany, previousRepresentation.getLegalRepCompany());
        assertEquals(legalRepReferenceNumber, previousRepresentation.getLegalRepReferenceNumber());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new PreviousRepresentation(
            null,
            legalRepReferenceNumber))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreviousRepresentation(
            legalRepCompany,
            null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

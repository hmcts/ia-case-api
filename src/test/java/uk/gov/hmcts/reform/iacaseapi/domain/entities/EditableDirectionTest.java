package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EditableDirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";

    private EditableDirection editableDirection = new EditableDirection(
        explanation,
        parties,
        dateDue
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(explanation, editableDirection.getExplanation());
        assertEquals(parties, editableDirection.getParties());
        assertEquals(dateDue, editableDirection.getDateDue());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new EditableDirection(null, parties, dateDue))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new EditableDirection(explanation, null, dateDue))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new EditableDirection(explanation, parties, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

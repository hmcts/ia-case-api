package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EditableDirectionTest {

    final String explanation = "Do the thing";
    final Parties parties = Parties.RESPONDENT;
    final String dateDue = "2018-12-31T12:34:56";

    EditableDirection editableDirection = new EditableDirection(
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

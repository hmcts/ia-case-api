package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class PreviousDatesTest {

    private final String dateDue = "2019-12-01";
    private final String dateSent = "2018-12-01T12:34:56";


    private PreviousDates previousDates = new PreviousDates(
        dateDue,
        dateSent
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(dateDue, previousDates.getDateDue());
        assertEquals(dateSent, previousDates.getDateSent());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new PreviousDates(null, dateSent))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreviousDates(dateDue, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
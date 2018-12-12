package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;

public class AppealReferenceNumberTest {

    @Test
    public void generates_appeal_reference_number_from_object() {
        AppealReferenceNumber appealReferenceNumber = new AppealReferenceNumber("RP/50000/2019");

        assertThat(appealReferenceNumber.getType(), is(AsylumAppealType.RP));
        assertThat(appealReferenceNumber.getSequence(), is("50000"));
        assertThat(appealReferenceNumber.getYear(), is("2019"));
    }
}
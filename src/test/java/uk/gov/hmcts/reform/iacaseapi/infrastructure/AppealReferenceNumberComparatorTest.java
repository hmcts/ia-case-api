package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;

import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;

public class AppealReferenceNumberComparatorTest {

    @Test
    public void compares_appeal_reference_numbers_correctly_with_the_same_sequence_number() {
        AppealReferenceNumberComparator underTest = new AppealReferenceNumberComparator();

        int expectedZero = underTest.compare(
                new AppealReferenceNumber(PA, 1, "2018"),
                new AppealReferenceNumber(PA, 1, "2018"));

        assertThat(expectedZero).isEqualTo(0);

        int expectedNegative = underTest.compare(
                new AppealReferenceNumber(PA, 1, "2018"),
                new AppealReferenceNumber(PA, 1, "2017"));

        assertThat(expectedNegative).isEqualTo(-1);

        int equalPositive = underTest.compare(
                new AppealReferenceNumber(PA, 1, "2018"),
                new AppealReferenceNumber(PA, 1, "2019"));

        assertThat(equalPositive).isEqualTo(1);
    }

    @Test
    public void compares_appeal_reference_numbers_correctly_with_the_same_year() {
        AppealReferenceNumberComparator underTest = new AppealReferenceNumberComparator();

        int expectedZero = underTest.compare(
                new AppealReferenceNumber(PA, 1, "2018"),
                new AppealReferenceNumber(PA, 1, "2018"));

        assertThat(expectedZero).isEqualTo(0);

        int expectedNegative = underTest.compare(
                new AppealReferenceNumber(PA, 2, "2018"),
                new AppealReferenceNumber(PA, 1, "2018"));

        assertThat(expectedNegative).isEqualTo(-1);

        int equalPositive = underTest.compare(
                new AppealReferenceNumber(PA, 1, "2018"),
                new AppealReferenceNumber(PA, 2, "2018"));

        assertThat(equalPositive).isEqualTo(1);
    }
}
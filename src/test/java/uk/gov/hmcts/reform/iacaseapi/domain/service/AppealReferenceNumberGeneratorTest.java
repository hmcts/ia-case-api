package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.RP;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.AppealReferenceNumberInitializerFromCcd;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealReferenceNumberGeneratorTest {

    private final DateProvider dateProvider = mock(DateProvider.class);

    private final AppealReferenceNumberInitializerFromCcd appealReferenceNumberInitializer =
            mock(AppealReferenceNumberInitializerFromCcd.class);

    private final AppealReferenceNumberGenerator underTest =
            new AppealReferenceNumberGenerator(
                    "50000",
                    appealReferenceNumberInitializer,
                    dateProvider);

    @Before
    public void setUp() {
        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    public void increments_protection_appeal_reference_number() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(initialReferenceNumbers());

        Optional<String> maybeAppealReferenceNumber =
                underTest.getNextAppealReferenceNumberFor(PA.getValue());

        assertThat(maybeAppealReferenceNumber.get(), is("PA/50001/2018"));
    }

    @Test
    public void increments_revocation_of_protection_appeal_reference_number() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(initialReferenceNumbers());

        Optional<String> maybeAppealReferenceNumber =
                underTest.getNextAppealReferenceNumberFor(RP.getValue());

        assertThat(maybeAppealReferenceNumber.get(), is("RP/50001/2018"));
    }

    @Test
    public void skips_initialization_when_already_initialized() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(initialReferenceNumbers());

        underTest.getNextAppealReferenceNumberFor(RP.getValue());
        underTest.getNextAppealReferenceNumberFor(RP.getValue());

        verify(appealReferenceNumberInitializer, times(1)).initialize();
    }

    @Test
    public void resets_sequence_on_year_increment_for_revocation_of_appeal_reference() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(someAppealReferenceNumbersTowardsTHeEndOf2018()); // ref numbers in 2018
        when(dateProvider.now()).thenReturn(LocalDate.now().withYear(2019));

        Optional<String> maybeAppealReferenceNumber = underTest.getNextAppealReferenceNumberFor(RP.getValue());

        assertThat(maybeAppealReferenceNumber.get(), is("RP/50001/2019"));
    }

    @Test
    public void resets_sequence_on_year_increment_for_protection_appeal_reference() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(someAppealReferenceNumbersTowardsTHeEndOf2018()); // ref numbers in 2018
        when(dateProvider.now()).thenReturn(LocalDate.now().withYear(2019));

        Optional<String> maybeAppealReferenceNumber = underTest.getNextAppealReferenceNumberFor(PA.getValue());

        assertThat(maybeAppealReferenceNumber.get(), is("PA/50001/2019"));
    }

    @Test
    public void handles_unknown_appeal_type() {

        when(appealReferenceNumberInitializer.initialize()).thenReturn(initialReferenceNumbers());; // ref numbers in 2018

        Optional<String> maybeAppealReferenceNumber = underTest.getNextAppealReferenceNumberFor("some_unknown_appeal_type");

        assertThat(maybeAppealReferenceNumber, is(Optional.empty()));
    }

    private Map<AsylumAppealType, AppealReferenceNumber> initialReferenceNumbers() {
        Map<AsylumAppealType, AppealReferenceNumber> referenceNumbers = new HashMap<>();
        referenceNumbers.put(PA, new AppealReferenceNumber(PA, "50000", "2018"));
        referenceNumbers.put(RP, new AppealReferenceNumber(RP, "50000", "2018"));
        return referenceNumbers;
    }

    private Map<AsylumAppealType, AppealReferenceNumber> someAppealReferenceNumbersTowardsTHeEndOf2018() {
        Map<AsylumAppealType, AppealReferenceNumber> referenceNumbers = new HashMap<>();
        referenceNumbers.put(PA, new AppealReferenceNumber(PA, "50201", "2018"));
        referenceNumbers.put(RP, new AppealReferenceNumber(RP, "50201", "2018"));
        return referenceNumbers;
    }
}
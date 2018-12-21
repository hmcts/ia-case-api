package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.RP;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.AsylumCaseRetrievalException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CoreCaseDataRetriever;

public class AppealReferenceSequenceInitializerFromCcdTest {

    private final int appealReferenceSequenceSeed = 50000;
    private final CoreCaseDataRetriever coreCaseDataRetriever = mock(CoreCaseDataRetriever.class);
    private final SystemDateProvider systemDateProvider = mock(SystemDateProvider.class);
    private AppealReferenceNumberInitializerFromCcd underTest;

    @Before
    public void setUp() {

        when((systemDateProvider.now())).thenReturn(now().withYear(2018));

        underTest = new AppealReferenceNumberInitializerFromCcd(
                coreCaseDataRetriever,
                systemDateProvider,
                appealReferenceSequenceSeed);
    }

    @Test
    public void throws_when_client_fails() {

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted())
                .thenThrow(AsylumCaseRetrievalException.class);

        assertThatThrownBy(() -> underTest.initialize())
                .isExactlyInstanceOf(AsylumCaseRetrievalException.class);
    }


    @Test
    public void uses_sequence_seed_when_ccd_returns_zero_cases() {

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted()).thenReturn(emptyList());

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(PA).getSequence(), is(appealReferenceSequenceSeed));
    }

    @Test
    public void initializes_reference_numbers_based_on_case_list_from_Ccd() {

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted()).thenReturn(someListOf(
                "RP/50001/2018",
                "RP/50020/2018", // Latest RP reference
                "PA/50401/2018", // Latest PA reference
                "PA/50301/2018",
                "RP/50501/2017",
                "RP/53001/2017",
                "PA/50401/2017",
                "PA/53001/2017"
        ));

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is("50020"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is("50401"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));
    }

    @Test
    public void silently_handles_case_from_ccd_that_has_no_case_data() {

        List<Map> cases = someListOf(
                "RP/50001/2018",
                "RP/50020/2018", // Latest RP reference
                "PA/50401/2018", // Latest PA reference
                "PA/50301/2018",
                "RP/50501/2017",
                "RP/53001/2017",
                "PA/50401/2017",
                "PA/53001/2017");

        cases.add(emptyMap());

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted())
                .thenReturn(cases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is("50020"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is("50401"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));
    }

    @Test
    public void silently_handles_case_from_ccd_that_has_no_reference_number() {

        List<Map> cases = someListOf(
                "RP/50001/2018",
                "RP/50020/2018", // Latest RP reference
                "PA/50401/2018", // Latest PA reference
                "PA/50301/2018",
                "RP/50501/2017",
                "RP/53001/2017",
                "PA/50401/2017",
                "PA/53001/2017");

        cases.add(singletonMap("case_data", emptyMap()));

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted())
                .thenReturn(cases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is("50020"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is("50401"));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));
    }

    @Test
    public void uses_seed_when_list_of_cases_that_have_no_reference_numbers() {

        when(coreCaseDataRetriever.retrieveAppealCasesInAllStatesExceptAppealStarted())
                .thenReturn(singletonList(singletonMap("case_data", emptyMap())));

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(PA).getSequence(), is(appealReferenceSequenceSeed));
    }

    private List<Map> someListOf(String... referenceNumbers) {
        return stream(referenceNumbers)
                .map(ref -> singletonMap("case_data", singletonMap("appealReferenceNumber", ref)))
                .collect(toList());
    }

}
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType.RP;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberInitializerException;
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

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenThrow(AppealReferenceNumberInitializerException.class);

        assertThatThrownBy(() -> underTest.initialize())
            .isExactlyInstanceOf(AppealReferenceNumberInitializerException.class);
    }

    @Test
    public void uses_sequence_seed_when_ccd_returns_zero_cases() {

        when(coreCaseDataRetriever.retrieveAllAppealCases()).thenReturn(emptyList());

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(PA).getSequence(), is(appealReferenceSequenceSeed));
    }

    @Test
    public void initializes_reference_numbers_based_on_case_list_from_Ccd() {

        when(coreCaseDataRetriever.retrieveAllAppealCases()).thenReturn(someListOf(
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

        assertThat(referenceNumberMap.get(RP).getSequence(), is(50020));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is(50401));
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

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenReturn(cases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(50020));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is(50401));
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

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenReturn(cases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(50020));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is(50401));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));
    }

    @Test
    public void uses_seed_when_list_of_cases_that_have_no_reference_numbers() {

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenReturn(singletonList(singletonMap("case_data", emptyMap())));

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(PA).getSequence(), is(appealReferenceSequenceSeed));
    }

    @Test
    public void handle_case_where_ccd_has_RP_cases_but_not_PA_cases() {

        List<Map> onlyRpAppealCases = someListOf(
            "RP/50001/2018",
            "RP/50020/2018", // Latest RP reference
            "RP/53001/2017");

        onlyRpAppealCases.add(singletonMap("case_data", emptyMap()));

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenReturn(onlyRpAppealCases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(RP).getSequence(), is(50020));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(PA).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(PA).getYear(), is("2018"));
    }

    @Test
    public void handle_case_where_ccd_has_PA_cases_but_not_RP_cases() {

        List<Map> onlyRpAppealCases = someListOf(
            "PA/50001/2018",
            "PA/50020/2018", // Latest RP reference
            "PA/53001/2017");

        onlyRpAppealCases.add(singletonMap("case_data", emptyMap()));

        when(coreCaseDataRetriever.retrieveAllAppealCases())
            .thenReturn(onlyRpAppealCases);

        Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap = underTest.initialize();

        assertThat(referenceNumberMap.get(PA).getSequence(), is(50020));
        assertThat(referenceNumberMap.get(PA).getYear(), is("2018"));

        assertThat(referenceNumberMap.get(RP).getSequence(), is(appealReferenceSequenceSeed));
        assertThat(referenceNumberMap.get(RP).getYear(), is("2018"));
    }

    private List<Map> someListOf(String... referenceNumbers) {
        return stream(referenceNumbers)
            .map(ref -> singletonMap("case_data", singletonMap("appealReferenceNumber", ref)))
            .collect(toList());
    }
}

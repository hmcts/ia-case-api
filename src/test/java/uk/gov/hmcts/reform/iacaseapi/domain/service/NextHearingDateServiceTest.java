package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NextHearingDateServiceTest {

    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private NextHearingDateService nextHearingDateService;

    private final NextHearingDetails nextHearingDetailsFromHearings = NextHearingDetails
        .builder()
        .hearingId("hearingId")
        .hearingDateTime(LocalDateTime.now().toString())
        .build();
    private final String listCaseHearingDate = LocalDateTime.now().plusDays(1).toString();
    private final NextHearingDetails nextHearingDetailsFromCaseData = NextHearingDetails
        .builder()
        .hearingId("999")
        .hearingDateTime(listCaseHearingDate)
        .build();

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        nextHearingDateService = new NextHearingDateService(iaHearingsApiService, featureToggler);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void test_enabled(boolean enabled) {
        when(featureToggler.getValue("nextHearingDateEnabled", false)).thenReturn(enabled);

        assertEquals(enabled, nextHearingDateService.enabled());
    }

    @Test
    public void test_calculateNextHearingDateFromHearings() {
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class))
            .thenReturn(Optional.of(nextHearingDetailsFromHearings));

        NextHearingDetails nextHearingDetails =
            nextHearingDateService.calculateNextHearingDateFromHearings(callback, ABOUT_TO_START);

        assertNotNull(nextHearingDetails);

        assertEquals(nextHearingDetailsFromHearings, nextHearingDetails);
    }

    @Test
    public void should_get_next_hearing_date_from_case_data_when_calculating_from_hearings_fails() {
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));

        NextHearingDetails nextHearingDetails =
            nextHearingDateService.calculateNextHearingDateFromHearings(callback, ABOUT_TO_START);

        assertNotNull(nextHearingDetails);

        assertEquals(nextHearingDetailsFromCaseData, nextHearingDetails);
    }

    @Test
    public void should_get_next_hearing_date_from_case_data_when_calculating_from_hearings_throws_exception() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(iaHearingsApiService.aboutToStart(callback))
            .thenThrow(new ServiceResponseException("error message", null));

        NextHearingDetails nextHearingDetails =
            nextHearingDateService.calculateNextHearingDateFromHearings(callback, ABOUT_TO_START);

        assertNotNull(nextHearingDetails);

        assertEquals(nextHearingDetailsFromCaseData, nextHearingDetails);
    }

    @Test
    public void test_calculateNextHearingDateFromCaseData() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));

        NextHearingDetails nextHearingDetails =
            nextHearingDateService.calculateNextHearingDateFromCaseData(callback);

        assertNotNull(nextHearingDetails);

        assertEquals(nextHearingDetailsFromCaseData, nextHearingDetails);
    }

    @Test
    public void test_clearHearingDateInformation() {
        AsylumCase asylumCase = new AsylumCase();
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingDateTime("01/12/2024")
            .hearingId("1234")
            .build();
        asylumCase.write(LIST_CASE_HEARING_DATE, LocalDateTime.now().toString());
        asylumCase.write(NEXT_HEARING_DETAILS, nextHearingDetails);

        nextHearingDateService.clearHearingDateInformation(asylumCase);

        NextHearingDetails cleared = asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class).orElse(null);

        assertNotNull(cleared);
        assertNull(cleared.getHearingDateTime());
        assertNull(cleared.getHearingId());
        assertTrue(asylumCase.read(LIST_CASE_HEARING_DATE).isEmpty());
    }
}

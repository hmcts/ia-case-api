package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
public class RequestCaseBuildingPreparerTest {

    private static final int DUE_IN_DAYS = 28;
    private static final int DUE_IN_DAYS_FROM_SUBMISSION_DATE = 42;

    @Mock private DateProvider dateProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<String> asylumCaseValuesArgumentCaptor;
    @Captor private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestCaseBuildingPreparer requestCaseBuildingPreparer;

    @Before
    public void setUp() {
        requestCaseBuildingPreparer =
                new RequestCaseBuildingPreparer(DUE_IN_DAYS, DUE_IN_DAYS_FROM_SUBMISSION_DATE, dateProvider);
    }

    @Test
    public void should_prepare_send_direction_fields() {

        final String expectedExplanationContains = "You have 28 days after the respondent’s bundle is provided, or 42 days after the Notice of Appeal, whichever is the later, to upload your Appeal Skeleton Argument and evidence";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDueDate = "2019-10-08";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-09-10"));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2019-08-10"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);


        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(3)).write(asylumExtractorCaptor.capture(), asylumCaseValuesArgumentCaptor.capture());

        List<AsylumCaseFieldDefinition> extractors = asylumExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = asylumCaseValuesArgumentCaptor.getAllValues();

        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)),
                containsString(expectedExplanationContains)
        );

        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_DATE_DUE)),
                containsString(expectedDueDate)
        );

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    public void handling_should_throw_if_cannot_actuall_handle() {
        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestCaseBuildingPreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_CASE_BUILDING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    public void should_not_allow_null_arugments() {
        assertThatThrownBy(() -> requestCaseBuildingPreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_submission_date_plus_42_days_when_submission_date_is_2_days_ago() {

        LocalDate submissionDateWithin42Days = LocalDate.now().minusDays(2);

        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(submissionDateWithin42Days.toString()));

        when(dateProvider.now()).thenReturn(LocalDate.now());

        final LocalDate buildCaseDirectionDueDate = RequestCaseBuildingPreparer.getBuildCaseDirectionDueDate(asylumCase, dateProvider, DUE_IN_DAYS_FROM_SUBMISSION_DATE, DUE_IN_DAYS);

        Assertions.assertThat(buildCaseDirectionDueDate).isEqualTo(submissionDateWithin42Days.plusDays(DUE_IN_DAYS_FROM_SUBMISSION_DATE));
    }

    @Test
    public void should_return_current_date_plus_28_days_when_submission_date_is_20_days_ago() {

        LocalDate submissionDateWithin42Days = LocalDate.now().minusDays(20);

        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(submissionDateWithin42Days.toString()));

        when(dateProvider.now()).thenReturn(LocalDate.now());

        final LocalDate buildCaseDirectionDueDate = RequestCaseBuildingPreparer.getBuildCaseDirectionDueDate(asylumCase, dateProvider, DUE_IN_DAYS_FROM_SUBMISSION_DATE, DUE_IN_DAYS);

        Assertions.assertThat(buildCaseDirectionDueDate).isEqualTo(LocalDate.now().plusDays(DUE_IN_DAYS));
    }

}

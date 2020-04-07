package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RecordLengthOfHearingPreparerHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private RecordLengthOfHearingPreparerHandler recordLengthOfHearingPreparerHandler;

    @Before
    public void setUp() {
        recordLengthOfHearingPreparerHandler = new RecordLengthOfHearingPreparerHandler();

        when(callback.getEvent()).thenReturn(Event.RECORD_LENGTH_OF_HEARING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_read_decision_rule_32_from_appellant() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("reheardRule32"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
        verify(asylumCase).read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class);
    }

    @Test
    public void should_read_decision_rule_35_from_appellant() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("reheardRule35"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
        verify(asylumCase).read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class);
    }

    @Test
    public void should_read_decision_rule_32_from_respondent() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("reheardRule32"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
        verify(asylumCase).read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class);
    }

    @Test
    public void should_read_decision_rule_35_from_respondent() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("reheardRule35"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
        verify(asylumCase).read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class);
    }

    @Test
    public void should_return_error_when_decision_partially_granted_from_respondent() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("partiallyGranted"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("Appeal can only be reheard due to appropriate Judge decision."));
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
        verify(asylumCase).read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class);
    }

    @Test
    public void  should_return_error_when_no_application_type() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("Appeal can only be reheard due to appropriate Judge decision."));
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(FTPA_APPLICANT_TYPE, String.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordLengthOfHearingPreparerHandler.canHandle(callbackStage, callback);

                if (event == Event.RECORD_LENGTH_OF_HEARING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordLengthOfHearingPreparerHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordLengthOfHearingPreparerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordLengthOfHearingPreparerHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordLengthOfHearingPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
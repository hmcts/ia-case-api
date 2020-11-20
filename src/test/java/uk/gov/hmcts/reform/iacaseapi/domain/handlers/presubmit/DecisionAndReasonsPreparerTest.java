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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DecisionAndReasonsPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;

    private DecisionAndReasonsPreparer decisionAndReasonsPreparer;

    @Before
    public void setUp() {

        decisionAndReasonsPreparer = new DecisionAndReasonsPreparer(featureToggler);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
    }

    @Test
    public void should_clear_existing_fields_when_set_aside_reheard_flag_exists() {

        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decisionAndReasonsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).clear(CASE_INTRODUCTION_DESCRIPTION);
        verify(asylumCase).clear(APPELLANT_CASE_SUMMARY_DESCRIPTION);
        verify(asylumCase).clear(IMMIGRATION_HISTORY_AGREEMENT);
        verify(asylumCase).clear(AGREED_IMMIGRATION_HISTORY_DESCRIPTION);
        verify(asylumCase).clear(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION);
        verify(asylumCase).clear(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
        verify(asylumCase).clear(SCHEDULE_OF_ISSUES_AGREEMENT);
        verify(asylumCase).clear(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION);
    }

    @Test
    public void should_hold_on_to_previous_fields_when_set_aside_reheard_flag_does_not_exist() {

        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decisionAndReasonsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(CASE_INTRODUCTION_DESCRIPTION);
        verify(asylumCase, times(0)).clear(APPELLANT_CASE_SUMMARY_DESCRIPTION);
        verify(asylumCase, times(0)).clear(IMMIGRATION_HISTORY_AGREEMENT);
        verify(asylumCase, times(0)).clear(AGREED_IMMIGRATION_HISTORY_DESCRIPTION);
        verify(asylumCase, times(0)).clear(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION);
        verify(asylumCase, times(0)).clear(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
        verify(asylumCase, times(0)).clear(SCHEDULE_OF_ISSUES_AGREEMENT);
        verify(asylumCase, times(0)).clear(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION);
    }

    @Test
    public void cannot_handle_if_feature_flag_disabled() {

        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        decisionAndReasonsPreparer = new DecisionAndReasonsPreparer(featureToggler);
        boolean canHandle = decisionAndReasonsPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decisionAndReasonsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = decisionAndReasonsPreparer.canHandle(callbackStage, callback);

                if (event == Event.DECISION_AND_REASONS_STARTED && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
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

        assertThatThrownBy(() -> decisionAndReasonsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionAndReasonsPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_AND_REASONS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecisionAndReasonsStartedSubStateProgressionTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AutoRequestHearingService autoRequestHearingService;

    private DecisionAndReasonsStartedSubStateProgression decisionAndReasonsStartSubStateProgression;

    @BeforeEach
    public void setUp() {
        decisionAndReasonsStartSubStateProgression =
            new DecisionAndReasonsStartedSubStateProgression(autoRequestHearingService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase)).thenReturn(true);
        when(autoRequestHearingService.makeAutoHearingRequest(callback, MANUAL_CREATE_HEARING_REQUIRED))
            .thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decisionAndReasonsStartSubStateProgression.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, callbackResponse.getData());
        verify(autoRequestHearingService, times(1))
            .makeAutoHearingRequest(callback, MANUAL_CREATE_HEARING_REQUIRED);
        verify(asylumCase, times(1)).write(DECISION_AND_REASONS_AVAILABLE, YesOrNo.NO);
        verify(asylumCase, times(1)).write(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED, YesOrNo.NO);
    }

    @Test
    void should_not_auto_request_hearing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decisionAndReasonsStartSubStateProgression.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, callbackResponse.getData());
        verify(autoRequestHearingService, never())
            .makeAutoHearingRequest(callback, MANUAL_CREATE_HEARING_REQUIRED);
        verify(asylumCase, times(1)).write(DECISION_AND_REASONS_AVAILABLE, YesOrNo.NO);
        verify(asylumCase, times(1)).write(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> decisionAndReasonsStartSubStateProgression.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.GENERATE_DECISION_AND_REASONS);
        assertThatThrownBy(
            () -> decisionAndReasonsStartSubStateProgression.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decisionAndReasonsStartSubStateProgression.canHandle(callbackStage, callback);

                if ((event == Event.DECISION_AND_REASONS_STARTED)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}

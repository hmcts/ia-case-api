package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewUpdateHearingRequirementsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ReviewUpdateHearingRequirementsHandler reviewUpdateHearingRequirementsHandler;

    @BeforeEach
    public void setup() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_ADJUSTMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.PREPARE_FOR_HEARING);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        reviewUpdateHearingRequirementsHandler =
            new ReviewUpdateHearingRequirementsHandler();
    }

    @Test
    void should_update_review_hearing_adjustments() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewUpdateHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.PREPARE_FOR_HEARING);
        verify(asylumCase, times(1)).clear(DISABLE_OVERVIEW_PAGE);
        verify(asylumCase, times(1)).clear(UPDATE_HEARING_REQUIREMENTS_EXISTS);
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.ADA_HEARING_ADJUSTMENTS_UPDATABLE);

        reset(callback);
        reset(asylumCase);
    }

    @Test
    void should_update_hearing_adjustment_responses() {
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Response to vulnerabilities"));
        when(asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class)).thenReturn(Optional.of("Granted"));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Response to remote call"));
        when(asylumCase.read(IS_REMOTE_HEARING_ALLOWED, String.class)).thenReturn(Optional.of("Granted"));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Response to multimedia"));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class)).thenReturn(Optional.of("Refused"));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Response to single sex court"));
        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Response to in camera court"));
        when(asylumCase.read(IS_IN_CAMERA_COURT_ALLOWED, String.class)).thenReturn(Optional.of("Refused"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                reviewUpdateHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(VULNERABILITIES_DECISION_FOR_DISPLAY, "Granted - Response to vulnerabilities");
        verify(asylumCase, times(1)).write(REMOTE_HEARING_DECISION_FOR_DISPLAY, "Granted - Response to remote call");
        verify(asylumCase, times(1)).write(MULTIMEDIA_DECISION_FOR_DISPLAY, "Refused - Response to multimedia");
        verify(asylumCase, never()).write(eq(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY), anyString());
        verify(asylumCase, times(1)).write(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, "Refused - Response to in camera court");
        verify(asylumCase, never()).write(eq(OTHER_DECISION_FOR_DISPLAY), anyString());
    }

    @Test
    void should_throw_error_if_cannot_handle_callback() {

        assertThatThrownBy(
            () -> reviewUpdateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(
            () -> reviewUpdateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = reviewUpdateHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_HEARING_ADJUSTMENTS && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsHandler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsHandler.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OTHER_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class ReviewDraftHearingRequirementsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private ReviewDraftHearingRequirementsHandler reviewDraftHearingRequirementsHandler;

    @BeforeEach
    public void setup() {
        reviewDraftHearingRequirementsHandler =
            new ReviewDraftHearingRequirementsHandler(featureToggler);
    }

    @Test
    void should_submit_review_hearing_requirements() {
        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        reset(callback);
        reset(asylumCase);
    }

    @Test
    void should_set_list_case_hearing_length_visible_field_for_reheard_appeal() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_not_set_list_case_hearing_length_visible_field_for_normal_appeal() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_not_set_list_case_hearing_length_visible_field_fwhen_feature_flag_disabled() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_update_hearing_adjustment_responses() {
        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
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
                reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_SUBMIT, callback);

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
            () -> reviewDraftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(
            () -> reviewDraftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = reviewDraftHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.REVIEW_HEARING_REQUIREMENTS && callbackStage == ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> reviewDraftHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsHandler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsHandler.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

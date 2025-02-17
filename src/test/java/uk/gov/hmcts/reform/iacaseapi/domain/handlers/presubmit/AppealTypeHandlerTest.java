package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealTypeForDisplay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AppealTypeHandlerTest {

    private static final String AGE_ASSESSMENT_PAGE_ID = "ageAssessment";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AppealTypeHandler appealTypeHandler;
    @Mock private FeatureToggler featureToggler;

    @BeforeEach
    void setup() {
        appealTypeHandler = new AppealTypeHandler(featureToggler);
    }

    @Test
    void set_to_earliest() {
        assertThat(appealTypeHandler.getDispatchPriority()).isEqualTo(EARLIEST);
    }

    @Test
    void should_set_appealType_to_ag_if_ageAssessment_yes_and_appellant_not_in_detention() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_TYPE, AppealType.AG);
        verify(asylumCase, times(1)).write(HAS_ADDED_LEGAL_REP_DETAILS, YES);
    }

    @Test
    void should_set_appealType_to_ag_if_ageAssessment_yes_and_not_ada() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_TYPE, AppealType.AG);
    }

    @Test
    void should_set_appealType_to_appealTypeForDisplay_if_ageAssessment_no_and_detained() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
            .thenReturn(Optional.of(AppealTypeForDisplay.HU));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(APPEAL_TYPE, AppealType.AG);
        verify(asylumCase, times(1))
            .write(APPEAL_TYPE, AppealType.from(AppealTypeForDisplay.HU.getValue()));
    }

    @Test
    void should_set_appealType_to_appealTypeForDisplay_if_ageAssessment_no_and_not_detained() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
                .thenReturn(Optional.of(AppealTypeForDisplay.HU));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(APPEAL_TYPE, AppealType.AG);
        verify(asylumCase, times(1))
                .write(APPEAL_TYPE, AppealType.from(AppealTypeForDisplay.HU.getValue()));
    }

    @Test
    void should_throw_error_if_appealTypeForDisplay_is_empty() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealTypeHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && List.of(START_APPEAL, EDIT_APPEAL).contains(callback.getEvent())
                ) {
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

        assertThatThrownBy(() -> appealTypeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealTypeHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealTypeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealTypeHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealTypeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    void should_not_write_appeal_type_if_naba_disabled() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPEAL_TYPE), any());
    }

    @Test
    void should_write_appeal_type_for_internal_detained_ejp_cases() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
            .thenReturn(Optional.of(AppealTypeForDisplay.HU));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(APPEAL_TYPE, AppealType.from(AppealTypeForDisplay.HU.getValue()));
    }

    @Test
    void should_rewrite_feature_toggle_flags_if_start_appeal() {
        // If event = start appeal, and feature toggle is off (Pre ccd release)
        // We want to make sure the Feature toggle values are saved on the cases.
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(featureToggler.getValue("naba-feature-flag", false)).thenReturn(false);
        when(featureToggler.getValue("naba-ada-feature-flag", false)).thenReturn(false);
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(false);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealTypeHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, times(1)).write(IS_NABA_ENABLED, NO);
        verify(asylumCase, times(1)).write(IS_NABA_ENABLED_OOC, NO);
        verify(asylumCase, times(1)).write(IS_NABA_ADA_ENABLED, NO);
        verify(asylumCase, times(1)).write(IS_OUT_OF_COUNTRY_ENABLED, NO);
    }
}

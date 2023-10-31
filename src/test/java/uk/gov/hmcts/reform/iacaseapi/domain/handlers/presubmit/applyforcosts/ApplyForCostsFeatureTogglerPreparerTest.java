package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_COSTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
class ApplyForCostsFeatureTogglerPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private ApplyForCostsFeatureTogglerPreparer applyForCostsFeatureTogglerPreparer;

    @BeforeEach
    public void setUp() {
        applyForCostsFeatureTogglerPreparer = new ApplyForCostsFeatureTogglerPreparer(featureToggler);
    }

    @Test
    void handler_checks_age_assessment_feature_flag_set_value() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("apply-for-costs-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
                applyForCostsFeatureTogglerPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void handler_checks_age_assessment_feature_flag_not_set_value() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("apply-for-costs-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
                applyForCostsFeatureTogglerPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).contains("The 'Apply for costs' feature is not enabled.");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = applyForCostsFeatureTogglerPreparer.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                        && (callback.getEvent() == APPLY_FOR_COSTS)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(START_APPEAL);
        assertThatThrownBy(() -> applyForCostsFeatureTogglerPreparer.handle(ABOUT_TO_START, callback))
                .isExactlyInstanceOf(IllegalStateException.class);

    }
}
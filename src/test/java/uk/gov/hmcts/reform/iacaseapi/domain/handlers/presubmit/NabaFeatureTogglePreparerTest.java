package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NABA_ADA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NABA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NABA_ENABLED_OOC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_OUT_OF_COUNTRY_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
class NabaFeatureTogglePreparerTest {
    
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private NabaFeatureTogglePreparer nabaFeatureTogglePreparer;

    @BeforeEach
    public void setUp() {

        nabaFeatureTogglePreparer = new NabaFeatureTogglePreparer(featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL" })
    void handler_checks_naba_ada_ooc_feature_flag_set_value(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("naba-feature-flag", false)).thenReturn(true);
        when(featureToggler.getValue("naba-ada-feature-flag", false)).thenReturn(true);
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
                nabaFeatureTogglePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        Mockito.verify(asylumCase, times(1)).write(
                IS_NABA_ENABLED, YesOrNo.YES);
        Mockito.verify(asylumCase, times(1)).write(
            IS_NABA_ENABLED_OOC, YesOrNo.YES);
        Mockito.verify(asylumCase, times(1)).write(
            IS_NABA_ADA_ENABLED, YesOrNo.YES);
        Mockito.verify(asylumCase, times(1)).write(
            IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL" })
    void handler_checks_naba_ada_flag_not_set(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
                nabaFeatureTogglePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        Mockito.verify(asylumCase, times(1)).write(
                IS_NABA_ENABLED, YesOrNo.NO);
        Mockito.verify(asylumCase, times(1)).write(
            IS_NABA_ENABLED_OOC, YesOrNo.NO);
        Mockito.verify(asylumCase, times(1)).write(
            IS_NABA_ADA_ENABLED, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = nabaFeatureTogglePreparer.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                        && (callback.getEvent() == START_APPEAL || callback.getEvent() == EDIT_APPEAL)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> nabaFeatureTogglePreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nabaFeatureTogglePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nabaFeatureTogglePreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nabaFeatureTogglePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> nabaFeatureTogglePreparer.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(START_APPEAL);
        assertThatThrownBy(() -> nabaFeatureTogglePreparer.handle(ABOUT_TO_START, callback))
                .isExactlyInstanceOf(NullPointerException.class);

    }
}
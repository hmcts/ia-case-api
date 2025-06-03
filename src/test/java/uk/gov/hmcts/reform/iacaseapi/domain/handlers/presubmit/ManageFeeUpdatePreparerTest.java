package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISPLAY_FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdatePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private FeatureToggler featureToggler;

    private ManageFeeUpdatePreparer manageFeeUpdatePreparer;

    @BeforeEach
    void setUp() {

        manageFeeUpdatePreparer = new ManageFeeUpdatePreparer(featureToggler);
    }

    @Test
    void handling_should_error_if_fee_update_not_required_last_completed() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved",
                "feeUpdateNotRequired"
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdatePreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You can no longer manage a fee update for this appeal "
                + "because a fee update has been recorded as not required.");
        verify(asylumCase, times(1)).write(DISPLAY_FEE_UPDATE_STATUS, YesOrNo.YES);

    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU"})
    void handling_should_error_for_payment_pending(AppealType type) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAYMENT_PENDING));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdatePreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You cannot manage a fee update for this appeal because the fee has not been paid yet");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU"})
    void handling_should_error_for_no_payment_status(AppealType type) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));

        assertThatThrownBy(() -> manageFeeUpdatePreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class).hasMessage("Payment status is not present");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"DC", "RP"})
    void handling_should_error_for_invalid_appeal_type(AppealType type) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdatePreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("You cannot manage a fee update for this appeal");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdatePreparer
            .handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = manageFeeUpdatePreparer.canHandle(callbackStage, callback);

                if ((event == Event.MANAGE_FEE_UPDATE)
                    && callbackStage == ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> manageFeeUpdatePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> manageFeeUpdatePreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

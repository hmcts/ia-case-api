package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISPLAY_FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.DECISION_TYPE_CHANGED;

import java.util.Arrays;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private FeatureToggler featureToggler;

    private ManageFeeUpdateHandler manageFeeUpdateHandler;

    @BeforeEach
    void setUp() {

        manageFeeUpdateHandler = new ManageFeeUpdateHandler(featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = FeeUpdateReason.class, names = {"DECISION_TYPE_CHANGED", "APPEAL_NOT_VALID", "FEE_REMISSION_CHANGED"})
    void handling_should_return_selected_fee_update_statuses(FeeUpdateReason feeUpdateReason) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Collections.singletonList(
                "Fee update recorded"
            ));

        final List<String> expectedFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded"
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(feeUpdateReason));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, expectedFeeUpdateStatus);
        verify(asylumCase, times(1)).write(DISPLAY_FEE_UPDATE_STATUS, YesOrNo.YES);
        if (feeUpdateReason.equals(DECISION_TYPE_CHANGED)) {
            verify(asylumCase, times(1)).write(CASE_ARGUMENT_AVAILABLE, YesOrNo.YES);
        } else {
            verify(asylumCase, times(0)).write(CASE_ARGUMENT_AVAILABLE, YesOrNo.YES);
        }
    }

    @Test
    void handling_should_return_selected_fee_update_statuses_after_fee_update_recorded() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.APPEAL_NOT_VALID));

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "Refund approved",
                "Fee update not required"
            ));

        final List<String> completedStagesFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded",
                "Refund approved"
            );
        final List<String> expectedFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded",
                "Refund approved",
                "Fee update not required"
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStagesFeeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, expectedFeeUpdateStatus);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = manageFeeUpdateHandler.canHandle(callbackStage, callback);

                if ((event == Event.MANAGE_FEE_UPDATE)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> manageFeeUpdateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_fee_reason_is_not_exists() {
        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Fee update reason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
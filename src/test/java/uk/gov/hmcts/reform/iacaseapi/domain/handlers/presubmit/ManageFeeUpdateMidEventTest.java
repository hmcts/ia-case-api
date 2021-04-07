package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateMidEventTest {

    @Mock private AsylumCase asylumCase;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private FeatureToggler featureToggler;

    private ManageFeeUpdateMidEvent manageFeeUpdateMidEvent;

    @BeforeEach
    void setUp() {

        manageFeeUpdateMidEvent = new ManageFeeUpdateMidEvent(featureToggler);
    }

    @Test
    void handling_should_error_for_amount_greater_than_zero_for_appeal_not_valid() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.APPEAL_NOT_VALID));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("1000"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("Appeal not valid is selected, the new fee amount must be 0");
    }

    @Test
    void handling_should_error_for_no_remission_decision() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You cannot choose this option because the remission request has not been decided or has been rejected");
    }

    @Test
    void handle_should_return_error_if_no_remission_exists_for_fee_remission_changed() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You cannot choose this option because there is no remission request associated with this appeal");

    }

    @Test
    void handling_should_error_if_fee_update_reason_is_not_present() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.APPEAL_NOT_VALID));

        assertThatThrownBy(() -> manageFeeUpdateMidEvent.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("New fee amount is not present");
    }

    @Test
    void handling_should_error_if_new_fee_amount_is_not_present() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        assertThatThrownBy(() -> manageFeeUpdateMidEvent.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Fee update reason is not present");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionDecision.class, names = { "APPROVED", "PARTIALLY_APPROVED" })
    void should_handle_if_the_remission_is_not_rejected(RemissionDecision remissionDecision) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("1000"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdateMidEvent
            .handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = manageFeeUpdateMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.MANAGE_FEE_UPDATE)
                    && callbackStage == MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> manageFeeUpdateMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> manageFeeUpdateMidEvent.handle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateMidEventTest {

    private static final String FEE_UPDATE_STATUS_INVALID_SIZE = "You cannot select more than one option at a time";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private FeatureToggler featureToggler;

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
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.APPEAL_NOT_VALID));
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
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains(
                "You cannot choose this option because the remission request has not been decided or has been rejected");
    }

    @Test
    void handle_should_return_error_if_remission_does_not_exists() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains(
                "You cannot choose this option because there is no remission request associated with this appeal");
    }

    @Test
    void handle_should_return_error_if_aip_journey_and_remission_does_not_exists() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
        when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(RemissionOption.NO_REMISSION));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains(
                "You cannot choose this option because there is no remission request associated with this appeal");
    }

    @Test
    void handle_should_return_error_on_one_or_no_completed_stages() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateAdditionalFeeRequested",
                "feeUpdateRefundApproved"
            ));
        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded"
            );


        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You cannot select more than one option at a time");

    }

    @Test
    void handle_should_return_error_if_no_remission_exists_for_fee_remission_changed() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains(
                "You cannot choose this option because there is no remission request associated with this appeal");

    }

    @Test
    void handling_should_error_if_fee_update_reason_is_not_present() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.APPEAL_NOT_VALID));

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
    @EnumSource(value = RemissionDecision.class, names = {"APPROVED", "PARTIALLY_APPROVED"})
    void should_handle_if_the_remission_is_not_rejected(RemissionDecision remissionDecision) {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.FEE_REMISSION_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("1000"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void handling_should_error_for_multiple_fee_update_status_selection_flow2() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded"
            );
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "Refund approved",
                "Fee update not required"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(FEE_UPDATE_STATUS_INVALID_SIZE);
    }

    @Test
    void handling_should_error_when_refund_instructed_before_refund_approved_stage() {

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundInstructed"
            ));
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded"
            );
        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You must select refund approved before you can mark a refund as instructed");
    }

    @Test
    void handling_throw_error_refund_instructed_no_valid_fee_update_status_selection() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "You cannot make this selection. Select either refund instructed or fee not required to continue.");
    }

    @Test
    void handling_throw_error_refund_instructed_invalid_fee_update_status_selection() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved",
                "feeUpdateAdditionalFeeRequested"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "You cannot make this selection. Select either refund instructed or fee not required to continue.");
    }

    @Test
    void handling_throw_error_refund_instructed_two_valid_fee_update_status_selection() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed",
                "feeUpdateNotRequired"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "You cannot make this selection. Select either refund instructed or fee not required to continue.");
    }

    @Test
    void handling_throw_error_refund_instructed_for_refund_approved_not_selected() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateNotRequired"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "This selection is not valid. You cannot deselect an option that has already been selected");
    }

    @Test
    void handling_throw_error_post_refund_instructed_for_value_not_selected() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "This selection is not valid. "
                + "You must select either fee update not required or additional fee requested to continue");
    }

    @Test
    void handling_throw_error_post_refund_instructed_for_many_value_selected() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed",
                "feeUpdateAdditionalFeeRequested",
                "feeUpdateNotRequired"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "This selection is not valid. "
                + "You must select either fee update not required or additional fee requested to continue");
    }

    @Test
    void handling_throw_error_post_refund_instructed_for_existing_value_not_selected() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed"
            );
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRefundApproved",
                "feeUpdateAdditionalFeeRequested"
            ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains(
            "This selection is not valid. You cannot deselect an option that has already been selected");
    }


    @ParameterizedTest
    @EnumSource(
        value = FeeUpdateReason.class,
        names = {"APPEAL_NOT_VALID", "FEE_REMISSION_CHANGED"})
    void handling_should_error_when_additional_fee_requested_for_wrong_appeal_type(FeeUpdateReason reason) {

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "feeUpdateAdditionalFeeRequested"
            ));
        final CheckValues<String> feeUpdateRecorded =
            new CheckValues<>(Arrays.asList(
                "feeUpdateRecorded"
            ));
        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded"
            );
        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class))
            .thenReturn(Optional.of(reason));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(
            Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of("0"));
        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateRecorded));
        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("You cannot select additional fee requested "
            + "for this type of fee update. It can only be a refund.");
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

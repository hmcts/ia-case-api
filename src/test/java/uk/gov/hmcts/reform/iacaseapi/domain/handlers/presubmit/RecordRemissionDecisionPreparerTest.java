package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordRemissionDecisionPreparerTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    @Mock private FeatureToggler featureToggler;
    @Mock private FeePayment<AsylumCase> feePayment;

    private RecordRemissionDecisionPreparer recordRemissionDecisionPreparer;

    @BeforeEach
    void setUp() {

        recordRemissionDecisionPreparer = new RecordRemissionDecisionPreparer(feePayment, featureToggler);
    }

    @Test
    void handle_should_return_error_if_no_remission_exists_to_record_remission_decision() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("You cannot record a remission decision because a remission has not been requested for this appeal");

    }

    @ParameterizedTest
    @MethodSource("appealWithRemissionTypesAndRemissionDecision")
    void should_return_error_for_remission_decision_is_present(
        AppealType type, RemissionType remissionType, RemissionDecision remissionDecision, AsylumCaseFieldDefinition field
    ) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("The remission decision for this appeal has already been recorded.");
    }

    private static Stream<Arguments> appealWithRemissionTypesAndRemissionDecision() {

        return Stream.of(
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, PARTIALLY_APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REJECTED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HO_WAIVER_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HO_WAIVER_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HO_WAIVER_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HO_WAIVER_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HO_WAIVER_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.HELP_WITH_FEES, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.HELP_WITH_FEES, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.HELP_WITH_FEES, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.HELP_WITH_FEES, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.HELP_WITH_FEES, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(AppealType.AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, APPROVED, LATE_REMISSION_TYPE)
        );
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "AG" })
    void should_set_fees_data_for_valid_appeal_types(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(feePayment.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_error_for_payment_status_paid(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).contains("The fee for this appeal has already been paid.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "RP" })
    void should_return_invalid_event_for_dc_and_rp_appeal_types(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("Record remission decision is not valid for the appeal type.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordRemissionDecisionPreparer.canHandle(callbackStage, callback);

                if ((event == Event.RECORD_REMISSION_DECISION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordRemissionDecisionPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}

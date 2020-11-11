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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

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

    @ParameterizedTest
    @MethodSource("shouldReturnErrorForRemissionDecisionIsPresentData")
    void should_return_error_for_remission_decision_is_present(RemissionDecision decision, AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(decision));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("The remission decision for this appeal has already been recorded.");
    }

    private static Stream<Arguments> shouldReturnErrorForRemissionDecisionIsPresentData() {

        return Stream.of(
            Arguments.of(APPROVED, AppealType.EA),
            Arguments.of(APPROVED, AppealType.HU),
            Arguments.of(APPROVED, AppealType.PA),
            Arguments.of(PARTIALLY_APPROVED, AppealType.EA),
            Arguments.of(PARTIALLY_APPROVED, AppealType.HU),
            Arguments.of(PARTIALLY_APPROVED, AppealType.PA),
            Arguments.of(REJECTED, AppealType.EA),
            Arguments.of(REJECTED, AppealType.HU),
            Arguments.of(REJECTED, AppealType.PA)
        );
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA" })
    void should_set_fees_data_for_valid_appeal_types(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(feePayment.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA" })
    void handle_should_error_for_payment_status_paid(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

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

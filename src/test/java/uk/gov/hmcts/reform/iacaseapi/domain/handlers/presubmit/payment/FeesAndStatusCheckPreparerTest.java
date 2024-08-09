package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FeesAndStatusCheckPreparerTest {

    private static final String PAYMENT_OPTION_NOT_AVAILABLE_LABEL = "The Make a payment option is not available.";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeePayment<AsylumCase> feePayment;
    @Mock
    private FeatureToggler featureToggler;
    private FeesAndStatusCheckPreparer feesAndStatusCheckPreparer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        feesAndStatusCheckPreparer =
            new FeesAndStatusCheckPreparer(true, featureToggler, feePayment);
    }

    private static Stream<Arguments> paymentOptionNotAvailableError() {
        return Stream.of(
            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, AppealType.EA),
            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, AppealType.EA),
            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, AppealType.EA),

            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, AppealType.EU),
            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, AppealType.EU),
            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, AppealType.EU),

            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, AppealType.HU),
            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, AppealType.HU),
            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, AppealType.HU),

            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.EA),
            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.EA),
            Arguments.of(RemissionType.HO_WAIVER_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.EA),

            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.PARTIALLY_APPROVED, AppealType.EU),
            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.PARTIALLY_APPROVED, AppealType.EU),
            Arguments.of(RemissionType.HELP_WITH_FEES, RemissionDecision.PARTIALLY_APPROVED, AppealType.EU),

            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.HU),
            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.HU),
            Arguments.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.PARTIALLY_APPROVED, AppealType.HU)
        );
    }

    @ParameterizedTest
    @MethodSource("paymentOptionNotAvailableError")
    void it_should_add_error_if_there_is_a_remission(RemissionType remissionType, RemissionDecision remissionDecision, AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(remissionDecision));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains(PAYMENT_OPTION_NOT_AVAILABLE_LABEL));
    }

    @Test
    void it_cannot_handle_callback_if_feepayment_not_enabled() {

        FeesAndStatusCheckPreparer fees =
            new FeesAndStatusCheckPreparer(
                false,
                featureToggler,
                feePayment
            );

        assertThatThrownBy(
            () -> fees.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feesAndStatusCheckPreparer.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL
                    || event == Event.PAYMENT_APPEAL)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "PAYMENT_APPEAL"
    })
    void should_write_feePaymentEnabled(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(feePayment.aboutToStart(callback)).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(IS_REMISSIONS_ENABLED, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "PAYMENT_APPEAL"
    })
    void should_return_not_available_error_for_old_pa_appeals(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(feePayment.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, AppealType.class)).thenReturn(Optional.empty());

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot make a payment for this appeal using Payment by Account"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feesAndStatusCheckPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesAndStatusCheckPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesAndStatusCheckPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @ParameterizedTest
    @ValueSource(strings = {"refusalOfEu", "refusalOfHumanRights", "protection", "euSettlementScheme"})
    void should_error_on_duplicate_payment_for_make_a_payment(String type) {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(AppealType.from(type));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Make a payment option is not available.");
    }

    @Test
    void should_error_on_pay_later_in_appeal_started_state_make_payment() {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_STARTED);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .containsAnyOf("The Make a payment option is not available.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"deprivation", "revocationOfProtection"})
    void should_error_on_make_a_payment_for_non_payment_appeal(String type) {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(AppealType.from(type));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("You do not have to pay for this type of appeal.");
    }

    private static Stream<Arguments> paymentOptionNotAvailable() {
        return Stream.of(
            Arguments.of(Optional.of(PaymentStatus.PAID), Optional.empty()),
            Arguments.of(Optional.of(PaymentStatus.PAID), Optional.of(RemissionDecision.APPROVED)),
            Arguments.of(Optional.of(PaymentStatus.PAID), Optional.of(RemissionDecision.PARTIALLY_APPROVED)),
            Arguments.of(Optional.of(PaymentStatus.PAYMENT_PENDING), Optional.of(RemissionDecision.APPROVED)),
            Arguments.of(Optional.of(PaymentStatus.PAYMENT_PENDING), Optional.of(RemissionDecision.PARTIALLY_APPROVED))
        );
    }

    @ParameterizedTest
    @MethodSource("paymentOptionNotAvailable")
    void should_error_when_payment_option_should_be_unavailable(
        Optional<PaymentStatus> paymentStatus,
        Optional<RemissionDecision> remissionDecision) {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(paymentStatus);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Make a payment option is not available.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"refusalOfEu", "refusalOfHumanRights", "protection", "euSettlementScheme"})
    void should_error_when_remission_approved_or_partially_approved(String type) {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(AppealType.from(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesAndStatusCheckPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Make a payment option is not available.");
    }

}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MarkPaymentPaidPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private MarkPaymentPaidPreparer markPaymentPaidPreparer;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        markPaymentPaidPreparer =
            new MarkPaymentPaidPreparer(true);
    }

    @Test
    void should_throw_error_if_appeal_type_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        assertThatThrownBy(() -> markPaymentPaidPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");

    }


    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "EA", "HU", "EU" })
    void should_throw_error_if_payment_status_is_already_paid(AppealType appealType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("The fee for this appeal has already been paid.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "EU" })
    void should_throw_error_if_payment_status_is_already_paid_for_non_remission_appeals(AppealType appealType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("The fee for this appeal has already been paid.");
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "EA", "HU", "EU" })
    void should_return_error_for_old_pa_ea_hu_cases(AppealType appealType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "RP", "DC" })
    void should_throw_error_for_non_payment_appeal(AppealType appealType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("Payment is not required for this type of appeal.");
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionTypes")
    void handling_should_error_for_remission_decision_not_present(AppealType type, RemissionType remissionType, AsylumCaseFieldDefinition field) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        Mockito.when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because the remission decision has not been recorded.");
    }

    private static Stream<Arguments> appealTypesWithRemissionTypes() {

        return Stream.of(
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE)
        );
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionApproved")
    void handling_should_error_for_remission_decision_approved(
        AppealType type, RemissionType remissionType, RemissionDecision remissionDecision, AsylumCaseFieldDefinition field) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because a full remission has been approved.");
    }

    private static Stream<Arguments> appealTypesWithRemissionApproved() {

        return Stream.of(
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE)
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> markPaymentPaidPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            Mockito.when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = markPaymentPaidPreparer.canHandle(callbackStage, callback);

                if ((event == Event.MARK_APPEAL_PAID)
                    && callbackStage == ABOUT_TO_START) {

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

        assertThatThrownBy(() -> markPaymentPaidPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markPaymentPaidPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markPaymentPaidPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markPaymentPaidPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAID_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MarkPaymentPaidStateHandlerTest {

    private static final String paymentDate = "2020-08-31";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    private MarkPaymentPaidStateHandler markPaymentPaidStateHandler;

    @BeforeEach
    public void setUp() {

        markPaymentPaidStateHandler =
                new MarkPaymentPaidStateHandler(true);
    }


    @ParameterizedTest
    @ValueSource(strings = {"refusalOfEu", "refusalOfHumanRights", "euSettlementScheme", "ageAssessment"})
    void should_mark_appeal_as_paid_and_state_change_for_ea_hu_eu_ag_appeals(String appealType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(AppealType.from(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(PAID_DATE, String.class))
            .thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_mark_appeal_as_paid_and_state_change_for_pa_appeals_only_in_pending_payment() {

        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(PAID_DATE, String.class))
            .thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_mark_appeal_as_paid_and_no_state_change_for_pa_appeals_() {

        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(PAID_DATE, String.class))
            .thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.CASE_BUILDING);

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_mark_appeal_as_paid_and_no_state_change_for_pa_appeals() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getState()).thenReturn(State.CASE_BUILDING);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(PAID_DATE, String.class))
            .thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.CASE_BUILDING);

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_throw_if_paid_date_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getState()).thenReturn(State.CASE_BUILDING);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAID_DATE, String.class))
            .thenReturn(Optional.of(paymentDate));

        assertThatThrownBy(() -> markPaymentPaidStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("AppealType is not present");
    }

    @Test
    void should_throw_if_appeal_type_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getState()).thenReturn(State.CASE_BUILDING);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        assertThatThrownBy(() -> markPaymentPaidStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Paid date is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = markPaymentPaidStateHandler.canHandle(callbackStage, callback);

                if ((event == Event.MARK_APPEAL_PAID || event == Event.PAYMENT_APPEAL)
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                ) {
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

        assertThatThrownBy(() -> markPaymentPaidStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markPaymentPaidStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_format_payment_date_for_aip_online_payment_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails().getState()).thenReturn(State.PENDING_PAYMENT);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PAYMENT_DATE, String.class)).thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);

        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
                .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_not_write_payment_date_for_aip_online_payment_appeal_when_payment_date_absent() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails().getState()).thenReturn(State.PENDING_PAYMENT);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PAYMENT_DATE, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, never()).write(eq(PAYMENT_DATE), any());
    }

    @Test
    void should_format_payment_date_for_aip_offline_mark_appeal_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getState()).thenReturn(State.PENDING_PAYMENT);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PAID_DATE, String.class)).thenReturn(Optional.of(paymentDate));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                markPaymentPaidStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);

        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAID);
        verify(asylumCase, times(1))
                .write(PAYMENT_DATE, LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }

    @Test
    void should_throw_if_paid_date_not_present_for_aip_offline_mark_appeal_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getState()).thenReturn(State.PENDING_PAYMENT);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PAID_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markPaymentPaidStateHandler
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Paid date is not present");
    }

}



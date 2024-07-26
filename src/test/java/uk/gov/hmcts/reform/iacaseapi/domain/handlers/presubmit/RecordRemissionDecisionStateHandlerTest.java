package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class RecordRemissionDecisionStateHandlerTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @Mock private FeatureToggler featureToggler;
    @Mock private DateProvider dateProvider;
    @Mock private FeePayment feePayment;

    private RecordRemissionDecisionStateHandler recordRemissionDecisionStateHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        recordRemissionDecisionStateHandler = new RecordRemissionDecisionStateHandler(featureToggler, dateProvider, feePayment);
    }

    @Test
    void handling_should_throw_if_appeal_type_is_not_present() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handling_should_throw_if_remission_decision_is_not_present(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Remission decision is not present");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "EU", "AG" })
    void should_return_appeal_submitted_state_on_remission_approved_for_ea_hu_eu_ag(AppealType type) {
        // and service-request tab should be hidden (no payment to take care of)
        // and markAppealAsPaid should be hidden (no payment to take care of, case state already sorted)

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAID);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "EU", "AG" })
    void should_return_payment_pending_on_remission_partially_approved(AppealType type) {
        // and service-request tab should be hidden (payment is handled offline, waysToPay not yet supporting partial remissions)
        // and markAppealAsPaid should be visible, to allow admins to process offline payments

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
            LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @Test
    void should_return_appeal_submitted_state_on_remission_approved_for_pa() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.CASE_BUILDING);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAID);
    }

    @ParameterizedTest
    @CsvSource({ "EA, YES", "HU, YES", "PA, YES", "EU, YES", "AG, YES", "EA, NO", "HU, NO", "PA, NO", "EU, NO", "AG, NO" })
    void handle_should_return_payment_due_for_remission_rejected(AppealType type, String isAdmin) {
        // and service-request tab should be visible (payment is handled via waysToPay service-request)
        // and markAppealAsPaid should not be visible, (payment handled via waysToPay service-request)

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(isAdmin)));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verify(asylumCase,times(1)).write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
            LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, isAdmin.equals("NO") ? YesOrNo.YES : YesOrNo.NO);

    }

    @ParameterizedTest
    @CsvSource({ "EA, YES", "HU, YES", "PA, YES", "EU, YES", "AG, YES", "EA, NO", "HU, NO", "PA, NO", "EU, NO", "AG, NO" })
    void handle_should_leave_payment_status_as_is_if_present_for_remission_rejected(AppealType type, String isAdmin) {
        // payment status gets left alone (e.g. when appeal gets paid for, THEN remissions are requested and decided)

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(isAdmin)));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAID);
        verify(asylumCase,times(1)).write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
            LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, isAdmin.equals("NO") ? YesOrNo.YES : YesOrNo.NO);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordRemissionDecisionStateHandler.canHandle(callbackStage, callback);

                if ((event == Event.RECORD_REMISSION_DECISION)
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

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

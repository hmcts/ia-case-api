package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_REJECTED_DATE_PLUS_14DAYS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PENDING_PAYMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
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
    @MethodSource("typeRemissionOptionAndHelpWithFees")
    void should_return_appeal_submitted_state_on_remission_approved_for_ea_hu_eu_ag(AppealType type, State state, State newState) {
        // and service-request tab should be hidden (no payment to take care of)
        // and markAppealAsPaid should be hidden (no payment to take care of, case state already sorted)

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(newState);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAID);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
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
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_call_fee_payment_for_lr_journey_when_remission_rejected(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verify(asylumCase, times(1)).write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
            LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_for_internal_case_when_remission_rejected(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_for_aip_journey_when_remission_rejected(AppealType type) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_when_appellants_represented_for_remission_rejected(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_call_fee_payment_for_lr_journey_when_remission_partially_approved(AppealType type) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verify(asylumCase, times(1)).write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
            LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_for_internal_case_when_remission_partially_approved(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_for_aip_journey_when_remission_partially_approved(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA", "EU", "AG" })
    void handle_should_not_call_fee_payment_when_appellants_represented_for_remission_partially_approved(AppealType type) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.PENDING_PAYMENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            recordRemissionDecisionStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(returnedCallbackResponse).isNotNull();
        assertThat(returnedCallbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(feePayment, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);
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

    private static Stream<Arguments> typeRemissionOptionAndHelpWithFees() {
        return Stream.of(
                Arguments.of(EA, PENDING_PAYMENT, APPEAL_SUBMITTED),
                Arguments.of(EA, DECISION, DECISION),

                Arguments.of(HU, PENDING_PAYMENT, APPEAL_SUBMITTED),
                Arguments.of(HU, DECISION, DECISION),

                Arguments.of(EU, PENDING_PAYMENT, APPEAL_SUBMITTED),
                Arguments.of(EU, DECISION, DECISION),

                Arguments.of(AG, PENDING_PAYMENT, APPEAL_SUBMITTED),
                Arguments.of(AG, DECISION, DECISION)
        );
    }
}

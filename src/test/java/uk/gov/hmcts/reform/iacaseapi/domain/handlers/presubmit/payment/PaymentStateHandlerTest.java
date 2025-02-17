package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment.PaymentStateHandler.PAY_LATER;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PaymentStateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private PaymentStateHandler paymentStateHandler;

    private static final String PA_PAY_NOW = "payNow";

    @Mock
    private FeatureToggler featureToggler;

    @BeforeEach
    public void setUp() {
        paymentStateHandler = new PaymentStateHandler(true, featureToggler);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
    }

    @Test
    void should_return_updated_state_for_pa_payment_pending_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"APPEAL_STARTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT"}, mode = EXCLUDE)
    void should_return_base_state_for_pa_pay_later_payment_appeal_valid_state_dlrm_flag_on(State state) {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(state);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"APPEAL_STARTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT"})
    void should_not_return_base_state_for_pa_pay_later_payment_appeal_invalid_state_dlrm_flag_on(State state) {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);

        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"APPEAL_STARTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT"}, mode = EXCLUDE)
    void should_return_base_state_for_pa_pay_later_payment_appeal_valid_state(State state) {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(state);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"APPEAL_STARTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT"})
    void should_not_return_base_state_for_pa_pay_later_payment_appeal_invalid_state(State state) {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @CsvSource({
        "payLater, PAYMENT_APPEAL, APPEAL_STARTED",
        "payLater, SUBMIT_APPEAL, RESPONDENT_REVIEW",
        "payNow, PAYMENT_APPEAL, RESPONDENT_REVIEW",
        "payLater, SUBMIT_APPEAL, APPEAL_STARTED",
        "payNow, PAYMENT_APPEAL, APPEAL_STARTED",
        "payNow, SUBMIT_APPEAL, RESPONDENT_REVIEW",
        "payNow, SUBMIT_APPEAL, APPEAL_STARTED",
    })
    void should_not_return_base_state_for_false_isValidPayLaterPaymentEvent(String paPayOption, Event event, State state) {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of(paPayOption));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = { "NO_REMISSION", "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION" })
    void should_return_updated_state_for_hu_remission_submit_as_payment_pending_state(RemissionType remissionType) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));
        asylumCase.write(REMISSION_TYPE, remissionType);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = { "NO_REMISSION", "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION" })
    void should_return_updated_state_for_ea_remission_submit_as_payment_pending_state(RemissionType remissionType) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));
        asylumCase.write(REMISSION_TYPE, remissionType);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = { "NO_REMISSION", "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION" })
    void should_return_updated_state_for_eu_remission_submit_as_payment_pending_state(RemissionType remissionType) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(EU));
        asylumCase.write(REMISSION_TYPE, remissionType);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "EU", "AG" })
    void should_return_ea_submit_as_appeal_submitted_state(AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    void should_return_updated_state_for_hu_payment_pending_submit_as_pending_payment_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    void should_return_updated_state_for_ea_payment_pending_submit_as_pending_payment_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    void should_return_updated_state_for_eu_payment_pending_submit_as_pending_payment_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(EU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "PA", "EU", "AG"})
    void should_return_valid_state_on_having_remissions_for_given_appeal_types(String type) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.HO_WAIVER_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU, AppealType.EU, AppealType.AG).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else if (Arrays.asList(AppealType.EA, AppealType.HU, AppealType.EU, AppealType.AG).contains(AppealType.valueOf(type))) {
            asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAID));
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "PA", "EU", "AG"})
    void should_return_valid_state_on_having_remissions_for_given_appeal_types_payment_failed(String type) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.FAILED));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.HO_WAIVER_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU, AppealType.EU, AppealType.AG).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "PA", "EU"})
    void should_return_valid_state_on_help_with_fees_for_given_appeal_types(String type) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.HELP_WITH_FEES);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        Assert.assertNotNull(returnedCallbackResponse);
        Assert.assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU, AppealType.EU, AppealType.AG).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        }
    }

    @ParameterizedTest
    @MethodSource("typeRemissionOptionAndHelpWithFees")
    void should_return_valid_state_on_help_with_fees_remissions_and_dlrm_fee_support_enabled_for_given_appeal_types(AppealType type, RemissionOption remissionOption, HelpWithFeesOption helpWithFeesOption, String  payLater) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(JOURNEY_TYPE, Optional.of(AIP));
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(type));
        asylumCase.write(REMISSION_TYPE, RemissionType.HELP_WITH_FEES);
        asylumCase.write(REMISSION_OPTION, Optional.of(remissionOption));
        asylumCase.write(HELP_WITH_FEES_OPTION, Optional.of(helpWithFeesOption));
        asylumCase.write(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, Optional.of(payLater));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        Assert.assertNotNull(returnedCallbackResponse);
        Assert.assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (hasRemission(remissionOption, helpWithFeesOption) && !payLater.equals(PAY_LATER)) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "PA", "EU"})
    void should_return_appeal_submitted_state_with_no_remission(String type) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.NO_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU, EU).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        }
    }

    @Test
    void should_return_updated_state_for_dc_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.DC));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    void should_return_updated_state_for_non_payment_rp_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.RP));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    void should_return_current_state_for_empty_appeal_type() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
            () -> paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
            () -> paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        assertThatThrownBy(
            () -> paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(asylumCase, never()).read(PAYMENT_STATUS);
    }

    @Test
    void it_can_handle_callback() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = paymentStateHandler.canHandle(callbackStage, callback);

                if ((Event.SUBMIT_APPEAL == event || Event.PAYMENT_APPEAL == event)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void cannot_handle_callback_when_fee_pay_disabled() {
        paymentStateHandler = new PaymentStateHandler(false, featureToggler);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = paymentStateHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> paymentStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_updated_state_for_pa_pay_now_payment_pending_submit_as_appeal_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));
        asylumCase.write(PA_APPEAL_TYPE_PAYMENT_OPTION, Optional.of(PA_PAY_NOW));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = { "NO_REMISSION", "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION" })
    void should_return_updated_state_for_pa_pay_now_remission_submit_as_appeal_submitted_state(RemissionType remissionType) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));
        asylumCase.write(REMISSION_TYPE, remissionType);
        asylumCase.write(PA_APPEAL_TYPE_PAYMENT_OPTION, Optional.of(PA_PAY_NOW));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            paymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    private static Stream<Arguments> typeRemissionOptionAndHelpWithFees() {
        return Stream.of(
            Arguments.of(EA, RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(EA, RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(EA, RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(EA, RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(EA, NO_REMISSION, WILL_PAY_FOR_APPEAL, ""),

            Arguments.of(HU, RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(HU, RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(HU, RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(HU, RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(HU, NO_REMISSION, WILL_PAY_FOR_APPEAL, ""),

            Arguments.of(PA, RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, PAY_LATER),
            Arguments.of(PA, RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, PAY_LATER),
            Arguments.of(PA, RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(PA, RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(PA, NO_REMISSION, WILL_PAY_FOR_APPEAL, PAY_LATER),

            Arguments.of(EU, RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(EU, RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(EU, RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, ""),
            Arguments.of(EU, RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, ""),
            Arguments.of(EU, NO_REMISSION, WILL_PAY_FOR_APPEAL, "")
        );
    }

    private boolean hasRemission(RemissionOption remissionOption, HelpWithFeesOption helpWithFeesOption) {
        return remissionOption != RemissionOption.NO_REMISSION
            || helpWithFeesOption != WILL_PAY_FOR_APPEAL;
    }
}

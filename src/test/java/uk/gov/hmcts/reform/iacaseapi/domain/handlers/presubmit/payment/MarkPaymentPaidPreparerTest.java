package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.AG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

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
    @Mock
    private FeatureToggler featureToggler;

    private MarkPaymentPaidPreparer markPaymentPaidPreparer;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        markPaymentPaidPreparer =
            new MarkPaymentPaidPreparer(true, featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = JourneyType.class)
    void should_throw_error_if_appeal_type_is_not_present(JourneyType journeyType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        assertThatThrownBy(() -> markPaymentPaidPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "PA,REP",
        "EA,REP",
        "HU,REP",
        "EU,REP",
        "AG,REP",
        "PA,AIP",
        "EA,AIP",
        "HU,AIP",
        "EU,AIP",
        "AG,AIP"
    })
    void should_throw_error_if_payment_status_is_already_paid(AppealType appealType, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("The fee for this appeal has already been paid.");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "EA,REP",
        "HU,REP",
        "EU,REP",
        "AG,REP",
        "EA,AIP",
        "HU,AIP",
        "EU,AIP",
        "AG,AIP"
    })
    void should_throw_error_if_payment_status_is_already_paid_for_non_remission_appeals(AppealType appealType, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("The fee for this appeal has already been paid.");
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "EA,REP",
        "HU,REP",
        "EU,REP",
        "EA,AIP",
        "HU,AIP",
        "EU,AIP"
    })
    void should_return_error_for_old_ea_hu_eu_cases(AppealType appealType, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid");
    }

    @ParameterizedTest
    @EnumSource(value = JourneyType.class)
    void should_return_error_for_old_pa_cases_when_is_lr_journey_and_dlrm_fee_remission_is_enabled(JourneyType journeyType) {
        Mockito.when(featureToggler.getValue("dlrm-fee-remission-feature-flag", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "DC,REP",
        "RP,REP",
        "DC,AIP",
        "RP,AIP"
    })
    void should_throw_error_for_non_payment_appeal(AppealType appealType, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("Payment is not required for this type of appeal.");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "PA,REP",
        "EA,REP",
        "HU,REP",
        "EU,REP",
        "PA,AIP",
        "EA,AIP",
        "HU,AIP",
        "EU,AIP"
    })
    void should_throw_error_for_ada_appeal(AppealType appealType, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("Payment is not required for this type of appeal.");
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionTypes")
    void handling_should_error_for_remission_decision_not_present(AppealType type, RemissionType remissionType, AsylumCaseFieldDefinition field, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because the remission decision has not been recorded.");
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionTypes")
    void handling_should_error_for_remission_decision_not_present_internal_detained_cases(AppealType type, RemissionType remissionType, AsylumCaseFieldDefinition field, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because the remission decision has not been recorded.");
    }

    private static Stream<Arguments> appealTypesWithRemissionTypes() {

        return Stream.of(
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, LATE_REMISSION_TYPE, AIP)
        );
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionApproved")
    void handling_should_error_for_remission_decision_approved(AppealType type, RemissionType remissionType, RemissionDecision remissionDecision, AsylumCaseFieldDefinition field, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because a full remission has been approved.");
    }

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionApproved")
    void handling_should_error_for_remission_decision_approved_internal_detained_cases(AppealType type, RemissionType remissionType, RemissionDecision remissionDecision, AsylumCaseFieldDefinition field, JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(type));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(field, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).contains("You cannot mark this appeal as paid because a full remission has been approved.");
    }

    private static Stream<Arguments> appealTypesWithRemissionApproved() {

        return Stream.of(
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, REP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(HU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(EU, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(PA, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HO_WAIVER_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.HELP_WITH_FEES, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP),
            Arguments.of(AG, RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION, RemissionDecision.APPROVED, LATE_REMISSION_TYPE, AIP)
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

    @ParameterizedTest
    @MethodSource("appealTypesWithRemissionOptions")
    void should_pass_checkRemissionConditions_if_remission_or_late_remission_present(AppealType appealType,
                                                                                     Optional<RemissionType> remissionType,
                                                                                     Optional<RemissionType> lateRemission,
                                                                                     JourneyType journeyType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(remissionType);
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(lateRemission);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            markPaymentPaidPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getErrors()).isEmpty();
    }

    private static Stream<Arguments> appealTypesWithRemissionOptions() {
        return Stream.of(
            Arguments.of(EA, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), REP),
            Arguments.of(EA, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(EA, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(HU, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), REP),
            Arguments.of(HU, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(HU, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(EU, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), REP),
            Arguments.of(EU, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(EU, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(PA, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), REP),
            Arguments.of(PA, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(PA, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(AG, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), REP),
            Arguments.of(AG, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(AG, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), REP),
            Arguments.of(EA, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), AIP),
            Arguments.of(EA, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(EA, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(HU, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), AIP),
            Arguments.of(HU, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(HU, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(EU, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), AIP),
            Arguments.of(EU, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(EU, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(PA, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), AIP),
            Arguments.of(PA, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(PA, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(AG, Optional.of(HO_WAIVER_REMISSION), Optional.empty(), AIP),
            Arguments.of(AG, Optional.empty(), Optional.of(HO_WAIVER_REMISSION), AIP),
            Arguments.of(AG, Optional.of(NO_REMISSION), Optional.of(HO_WAIVER_REMISSION), AIP)
        );
    }

}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DECISION_SELECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_REMISSIONS_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_LETTERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AiPFeesHandlerTest {

    @Mock
    private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;

    private AiPFeesHandler aiPFeesHandler;

    @BeforeEach
    public void setUp() {

        aiPFeesHandler =
                new AiPFeesHandler(true, feePayment, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_clear_remission_details_and_set_payment_data_when_pa_offline_payment(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_REMISSIONS_ENABLED, YesOrNo.NO);
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);

        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verifyRemissionsDetailsCleared();
    }

    @Test
    void should_not_write_payment_status_when_payment_status_is_not_empty() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
                .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(IS_REMISSIONS_ENABLED, YesOrNo.NO);
        verify(asylumCase, never()).write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU"})
    void should_clear_remission_data_for_ea_hu_eu_payments(AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
                .thenReturn(Optional.of("decisionWithoutHearing"));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);;
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verifyRemissionsDetailsCleared();
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "PA", "EU"})
    void should_clear_fee_and_remission_data_for_accelerated_detained_appeals(AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
                .thenReturn(Optional.of("decisionWithoutHearing"));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verifyNoInteractions(feePayment);
        verify(asylumCase, times(1))
                .write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");
        verifyFeeOptionDetailsCleared();
        verifyRemissionsDetailsCleared();
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"DC", "RP"})
    void should_clear_fee_payment_and_remission_data_for_non_payment_appeals(AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
                .thenReturn(Optional.of("decisionWithoutHearing"));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verifyNoInteractions(feePayment);
        verify(asylumCase, times(1))
                .write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");
        verify(asylumCase, times(1)).clear(PAYMENT_STATUS);
        verifyFeeOptionDetailsCleared();
        verifyRemissionsDetailsCleared();
    }

    @Test
    void should_write_remission_data_for_appeals_with_fee_waiver_from_home_office() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Home Office Waiver");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_OPTION);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    @Test
    void should_write_remission_data_for_appeals_with_asylum_support_from_home_office() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE));
        when(asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class)).thenReturn(Optional.of("123"));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_OPTION);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    @Test
    void should_not_clear_remission_data_when_asylum_support_reference_is_missing() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE));
        when(asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class)).thenReturn(Optional.empty());
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, never()).clear(HELP_WITH_FEES_OPTION);
        verify(asylumCase, never()).clear(HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    @ParameterizedTest
    @EnumSource(value = RemissionOption.class, names = {"UNDER_18_GET_SUPPORT", "PARENT_GET_SUPPORT"})
    void should_write_remission_data_for_appeals_with_local_authority_support(RemissionOption remissionOption) {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(remissionOption));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Local Authority Support");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_OPTION);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    @ParameterizedTest
    @MethodSource(value = "provideRemissionAndHelpWithFeeOptionData")
    void should_write_remission_data_for_appeals_with_help_with_fee(RemissionOption remissionOption,
                                                                    HelpWithFeesOption helpWithFeesOption) {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(remissionOption));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class))
                .thenReturn(Optional.of(helpWithFeesOption));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class))
                .thenReturn(Optional.of(HelpWithFeesOption.ALREADY_APPLIED));
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(REMISSION_OPTION, RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES);
        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Help with Fees");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(LOCAL_AUTHORITY_LETTERS);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    private static Stream<Arguments> provideRemissionAndHelpWithFeeOptionData() {
        return Stream.of(
                Arguments.of(RemissionOption.NO_REMISSION, HelpWithFeesOption.WANT_TO_APPLY),
                Arguments.of(RemissionOption.NO_REMISSION, HelpWithFeesOption.ALREADY_APPLIED),
                Arguments.of(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, HelpWithFeesOption.WANT_TO_APPLY),
                Arguments.of(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, HelpWithFeesOption.ALREADY_APPLIED)
        );
    }


    @Test
    void should_not_write_remission_data_when_no_remission_and_no_hwf() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.NO_REMISSION));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)).thenReturn(Optional.empty());
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, never()).write(REMISSION_OPTION, RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES);
        verify(asylumCase, never()).write(FEE_REMISSION_TYPE, "Help with Fees");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(LOCAL_AUTHORITY_LETTERS);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
        verifyFeeOptionDetailsCleared();
    }

    private void verifyFeeOptionDetailsCleared() {
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
    }

    private void verifyRemissionsDetailsCleared() {
        verify(asylumCase, times(1)).clear(REMISSION_OPTION);
        verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(LOCAL_AUTHORITY_LETTERS);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_OPTION);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REF_NUMBER);
    }


    @Test
    void should_return_fee_when_appeal_type_is_empty() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(JOURNEY_TYPE, JourneyType.class);
        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(feePayment, times(1)).aboutToSubmit(callback);
        verifyNoMoreInteractions(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void cannot_handle_callback_when_journey_type_is_not_aip(Event event) {
        aiPFeesHandler = new AiPFeesHandler(true, feePayment, featureToggler);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        assertFalse(aiPFeesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void cannot_handle_callback_when_fee_payment_feature_flag_is_disabled(Event event) {
        aiPFeesHandler = new AiPFeesHandler(false, feePayment, featureToggler);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertFalse(aiPFeesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(PreSubmitCallbackStage.class)
    void can_handle_when_callback_stage_is_about_to_submit(PreSubmitCallbackStage preSubmitCallbackStage) {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        if (preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
            assertTrue(aiPFeesHandler.canHandle(preSubmitCallbackStage, callback));
        } else {
            assertFalse(aiPFeesHandler.canHandle(preSubmitCallbackStage, callback));
        }
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void can_handle_callback_for_start_appeal_and_edit_appeal_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        if (event == Event.START_APPEAL || event == Event.EDIT_APPEAL) {
            assertTrue(aiPFeesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        } else {
            assertFalse(aiPFeesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> aiPFeesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

}
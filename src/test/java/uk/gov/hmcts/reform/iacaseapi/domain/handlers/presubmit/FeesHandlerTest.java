package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment.FeesHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FeesHandlerTest {

    @Mock private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;


    private FeesHandler feesHandler;

    @BeforeEach
    public void setUp() {

        feesHandler =
            new FeesHandler(true, feePayment, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

    }

    @Test
    void should_clear_other_when_pa_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.PA));
            when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));


            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
            verify(asylumCase, times(1)).write(IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
            verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
            verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
            verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
            verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
            verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_not_write_paymentPending_if_paymentStatus_not_empty() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(APPEAL_TYPE,
            AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);

        reset(callback);
        reset(feePayment);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EU", "EA", "HU", "AG" })
    void should_clear_other_when_eu_ea_hu_ag_offline_payment(String appealType) {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(APPEAL_TYPE,

                AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(appealType)));
            when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));


            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1))
                .write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
            verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
            reset(callback);
            reset(feePayment);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_clear_all_payment_details_for_non_payment_appeal_type(String type) {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));
            when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
            when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
                .thenReturn(Optional.of("decisionWithoutHearing"));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1))
                .write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");
            verify(asylumCase, times(1))
                .clear(HEARING_DECISION_SELECTED);
            verify(asylumCase, times(1))
                .clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
            verify(asylumCase, times(1))
                .clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
            verify(asylumCase, times(1))
                .clear(PAYMENT_STATUS);
            verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
            verify(asylumCase, times(1)).clear(REMISSION_TYPE);
            verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
            verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
            verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_return_remission_for_asylum_support() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("asylumSupport"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
    }

    @Test
    void should_return_remission_for_legal_aid() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("legalAid"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
    }

    @Test
    void should_not_return_remission_for_remissions_not_enabled() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(0)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
    }

    @Test
    void it_cannot_handle_callback_if_feepayment_not_enabled() {

        FeesHandler fees =
            new FeesHandler(true, feePayment, featureToggler);

        assertThatThrownBy(
            () -> fees.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @Disabled
    void it_can_handle_callback_for_aip_journey() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, never()).write(eq(IS_REMISSIONS_ENABLED), any());
    }

    private static Stream<Arguments> appealTypeNotEmptyOrNonAip() {
        return Stream.of(
            Arguments.of(Optional.of(JourneyType.AIP), Optional.of(AppealType.EA)),
            Arguments.of(Optional.empty(), Optional.of(AppealType.EA))
        );
    }

    @ParameterizedTest
    @MethodSource("appealTypeNotEmptyOrNonAip")
    @Disabled
    void it_runs_further_checks_if_not_aip_or_if_aip_and_appealType_already_present(
        Optional<JourneyType> optionalJourneyType,
        Optional<AppealType> optionalAppealType) {

        // this behavior makes sure the FeeHandler doesn't run several times but only at the beginning
        // of the AIP journey, when the appeal type hasn't been chosen yet

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(optionalJourneyType);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(optionalAppealType);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        // if journeyType isn't AIP and appealType isn't empty, then continue with what comes afterwards in the handler

        feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, atLeastOnce()).write(eq(IS_REMISSIONS_ENABLED), any());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        feesHandler = new FeesHandler(true, feePayment, featureToggler);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feesHandler.canHandle(callbackStage, callback);

                if ((callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    && (callback.getEvent() == Event.START_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void it_cannot_handle_callback_if_feePayment_not_enabled() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);
        feesHandler = new FeesHandler(false, feePayment, featureToggler);

        for (Event event : Event.values()) {
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = feesHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feesHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_default_to_option_with_hearing_for_missing_appeal_hearing_option(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(APPEAL_TYPE,
            AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));
        when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(DECISION_HEARING_FEE_OPTION, "decisionWithHearing");

    }

    @Test
    public void should_return_data_for_valid_asylumSupport_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("asylumSupport"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_legalAid_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("legalAid"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);

    }

    @Test
    public void should_return_data_for_valid_section17_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("section17"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 17");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_section20_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("section20"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 20");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_homeOfficeWaiver_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("homeOfficeWaiver"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Home Office fee waiver");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EA", "HU", "PA", "AG" })
    public void should_return_data_for_valid_help_with_fees_remission_type(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.valueOf(type)));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Help with Fees");
        verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EA", "HU", "PA", "AG" })
    public void should_return_data_for_valid_exceptional_circumstances_remission_type(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.valueOf(type)));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Exceptional circumstances");
        verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EA", "HU", "PA", "RP", "DC", "EU" })
    void should_clear_payments_for_all_ada_types(String appealType) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(appealType)));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feesHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");
        verify(asylumCase, times(1))
            .clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1))
            .clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1))
            .clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1))
            .clear(PAYMENT_STATUS);
        verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
        verify(asylumCase, times(1)).clear(REMISSION_TYPE);
        verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        reset(callback);
        reset(feePayment);
    }
}

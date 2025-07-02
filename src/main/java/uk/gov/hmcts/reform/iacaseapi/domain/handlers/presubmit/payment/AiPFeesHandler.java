package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_LETTERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

@Component
public class AiPFeesHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;
    private final FeatureToggler featureToggler;

    public AiPFeesHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment,
        FeatureToggler featureToggler
    ) {
        this.feePayment = feePayment;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Arrays.asList(
                Event.START_APPEAL,
                Event.EDIT_APPEAL
        ).contains(callback.getEvent())
                && isAipJourney(callback.getCaseDetails().getCaseData())
                && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isEmpty()) {
            return new PreSubmitCallbackResponse<>(feePayment.aboutToSubmit(callback));
        }

        YesOrNo isRemissionsEnabled
                = featureToggler.getValue("remissions-feature", false) ? YesOrNo.YES : YesOrNo.NO;
        asylumCase.write(IS_REMISSIONS_ENABLED, isRemissionsEnabled);

        switch (optionalAppealType.get()) {
            case EA:
            case HU:
            case PA:
            case EU:
                Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);

                if ((isAcceleratedDetainedAppeal.isPresent() && isAcceleratedDetainedAppeal.equals(Optional.of(YES))) || sourceOfAppealEjp(asylumCase)) {
                    // Accelerated Detained Appeals should be treated as RP/DC - no payment fees OR EJP appeals have no payments associated with them.
                    String hearingOption = asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class)
                            .orElse("decisionWithHearing");
                    asylumCase.write(DECISION_HEARING_FEE_OPTION, hearingOption);
                    asylumCase.clear(PAYMENT_STATUS);
                    clearFeeOptionDetails(asylumCase);
                    clearRemissionDetails(asylumCase);
                    break;
                }

                asylumCase = feePayment.aboutToSubmit(callback);

                Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
                if (isRemissionsEnabled == YES && remissionOption.isPresent()) {
                    setFeeRemissionTypeDetails(asylumCase);
                } else {
                    setFeePaymentDetails(asylumCase, optionalAppealType.get());
                    clearRemissionDetails(asylumCase);
                }

                asylumCase.write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED,
                        isfeePaymentEnabled ? YES : YesOrNo.NO);

                asylumCase.clear(RP_DC_APPEAL_HEARING_OPTION);
                break;

            case DC:
            case RP:
                // by default (before remissions feature integration) we choose decisionWithHearing
                // when the remissions are turned on it is a choice for the Legal Rep
                String hearingOption = asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class)
                        .orElse("decisionWithHearing");
                asylumCase.write(DECISION_HEARING_FEE_OPTION, hearingOption);
                asylumCase.clear(PAYMENT_STATUS);

                clearFeeOptionDetails(asylumCase);
                clearRemissionDetails(asylumCase);
                break;

            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setFeeRemissionTypeDetails(AsylumCase asylumCase) {

        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);

        if (remissionOption.isPresent()) {
            switch (remissionOption.get()) {
                case ASYLUM_SUPPORT_FROM_HOME_OFFICE:
                    asylumCase.write(FEE_REMISSION_TYPE, "Asylum support");
                    Optional<String> asylumSupportRefNumber = asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class);

                    if (asylumSupportRefNumber.isPresent()) {
                        asylumCase.clear(HELP_WITH_FEES_OPTION);
                        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    }
                    break;

                case FEE_WAIVER_FROM_HOME_OFFICE:
                    asylumCase.write(FEE_REMISSION_TYPE, "Home Office Waiver");
                    asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                    asylumCase.clear(HELP_WITH_FEES_OPTION);
                    asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    break;

                case UNDER_18_GET_SUPPORT:
                case PARENT_GET_SUPPORT:
                    asylumCase.write(FEE_REMISSION_TYPE, "Local Authority Support");
                    asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                    asylumCase.clear(HELP_WITH_FEES_OPTION);
                    asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    break;

                case NO_REMISSION:
                case I_WANT_TO_GET_HELP_WITH_FEES:
                    Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);

                    if (helpWithFeesOption.isEmpty() || helpWithFeesOption.get() == WILL_PAY_FOR_APPEAL) {
                        if (remissionOption.get() == RemissionOption.NO_REMISSION) {
                            clearRemissionDetails(asylumCase);
                        }
                    } else {
                        asylumCase.write(REMISSION_OPTION, RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES);
                        asylumCase.write(FEE_REMISSION_TYPE, "Help with Fees");
                        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
                    }
                    break;

                case RECEIVES_LEGAL_AID:
                    asylumCase.write(REMISSION_OPTION, RemissionOption.RECEIVES_LEGAL_AID);
                    asylumCase.write(FEE_REMISSION_TYPE, "Receives legal aid");
                    asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                    asylumCase.clear(HELP_WITH_FEES_OPTION);
                    asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
                    break;

                default:
                    break;

            }
            clearFeeOptionDetails(asylumCase);
        }

    }

    private void clearFeeOptionDetails(AsylumCase asylumCase) {
        asylumCase.clear(HEARING_DECISION_SELECTED);
        asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
    }

    private void setFeePaymentDetails(AsylumCase asylumCase, AppealType appealType) {

        if (asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).isEmpty()) {
            asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);
        }

        if (appealType == AppealType.PA) {
            asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        } else {
            asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        }
    }

    private void clearRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(REMISSION_OPTION);
        asylumCase.clear(FEE_REMISSION_TYPE);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HELP_WITH_FEES_OPTION);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
    }

}

package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeePaymentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;

    public FeePaymentHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment
    ) {
        this.feePayment = feePayment;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && Arrays.asList(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.PAYMENT_APPEAL
        ).contains(callback.getEvent())
               && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        YesOrNo isRemissionsEnabled = asylumCase.read(IS_REMISSIONS_ENABLED, YesOrNo.class)
            .orElse(YesOrNo.NO);

        switch (appealType) {
            case EA:
            case HU:
            case PA:
                Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                if (isRemissionsEnabled == YesOrNo.YES && optRemissionType.isPresent()
                    && optRemissionType.get() == HO_WAIVER_REMISSION) {

                    setFeeRemissionTypeDetails(asylumCase);
                } else if (isRemissionsEnabled == YesOrNo.YES && optRemissionType.isPresent()
                           && optRemissionType.get() == HELP_WITH_FEES) {

                    setHelpWithFeesDetails(asylumCase);
                } else {

                    asylumCase = feePayment.aboutToSubmit(callback);
                    setFeePaymentDetails(asylumCase, appealType);
                    clearRemissionDetails(asylumCase);
                }
                asylumCase.clear(RP_DC_APPEAL_HEARING_OPTION);
                break;

            case DC:
            case RP:
                String hearingOption = asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class)
                    .orElseThrow(() -> new IllegalStateException("Appeal hearing option is not present"));
                asylumCase.write(DECISION_HEARING_FEE_OPTION, hearingOption);
                asylumCase.clear(REMISSION_TYPE);
                asylumCase.clear(FEE_REMISSION_TYPE);
                clearFeeOptionDetails(asylumCase);
                clearRemissionDetails(asylumCase);
                break;

            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setFeeRemissionTypeDetails(AsylumCase asylumCase) {

        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class)
            .orElse("");
        switch (remissionClaim) {
            case "asylumSupport":
                asylumCase.write(FEE_REMISSION_TYPE, "Asylum support");
                asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
                asylumCase.clear(SECTION17_DOCUMENT);
                asylumCase.clear(SECTION20_DOCUMENT);
                asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
                break;

            case "legalAid":
                asylumCase.write(FEE_REMISSION_TYPE, "Legal Aid");
                asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
                asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
                asylumCase.clear(SECTION17_DOCUMENT);
                asylumCase.clear(SECTION20_DOCUMENT);
                asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
                break;

            case "section17":
                asylumCase.write(FEE_REMISSION_TYPE, "Section 17");
                asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
                asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
                asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
                asylumCase.clear(SECTION20_DOCUMENT);
                asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
                break;

            case "section20":
                asylumCase.write(FEE_REMISSION_TYPE, "Section 20");
                asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
                asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
                asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
                asylumCase.clear(SECTION17_DOCUMENT);
                asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
                break;

            case "homeOfficeWaiver":
                asylumCase.write(FEE_REMISSION_TYPE, "Home Office fee waiver");
                asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
                asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
                asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
                asylumCase.clear(SECTION17_DOCUMENT);
                asylumCase.clear(SECTION20_DOCUMENT);
                break;

            default:
                break;
        }

        asylumCase.clear(DECISION_HEARING_FEE_OPTION);
        clearFeeOptionDetails(asylumCase);
    }

    private void setHelpWithFeesDetails(AsylumCase asylumCase) {

        asylumCase.write(FEE_REMISSION_TYPE, "Help with Fees");
        asylumCase.clear(DECISION_HEARING_FEE_OPTION);
        clearRemissionDetails(asylumCase);
        clearFeeOptionDetails(asylumCase);

    }

    private void setFeePaymentDetails(AsylumCase asylumCase, AppealType appealType) {

        asylumCase.write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED,
            isfeePaymentEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (!asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).isPresent()) {
            asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);
        }

        if (appealType == AppealType.PA) {
            asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        } else {
            asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        }
        asylumCase.clear(FEE_REMISSION_TYPE);
    }

    private void clearFeeOptionDetails(AsylumCase asylumCase) {

        asylumCase.clear(HEARING_DECISION_SELECTED);
        asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        asylumCase.clear(PAYMENT_STATUS);
    }

    private void clearRemissionDetails(AsylumCase asylumCase) {

        asylumCase.clear(REMISSION_CLAIM);
        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
    }
}
